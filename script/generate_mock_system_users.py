#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 system_users 模拟数据的 SQL + 本地账本（JSON）。

- 身份证号：GB 11643-1999 校验位正确；行政区码为真实县级码；出生日为合法公历日。
- 库内不落任何“模拟”标记；仅账本文件可区分本次生成批次。
- username、nickname、realname 三者相同（中文姓名；批次内重名时姓名后追加数字保证 username 唯一）。
- SQL 使用 INSERT ... SELECT ... WHERE NOT EXISTS：username、nickname、realname、id_no、非空 mobile/email
  任一与同租户未删除行相同则跳过（空 mobile、空 email 不参与比较）；--minimal 时不校验无列的 realname/id_no。
- 默认邮箱为随机 **@qq.com / @163.com**；`--no-email` 时 email 为空串。

依赖：pip install bcrypt（用于与 Spring BCryptPasswordEncoder 兼容的密码哈希）

参考表字段：yudao-module-system ... AdminUserDO + TenantBaseDO/BaseDO
若库表尚未迁移扩展列，请加 --minimal（仅 ruoyi-vue-pro 基础列）。
加 --no-mobile 则不生成手机号，mobile 为空串，与现网空手机用户一致且不占用唯一索引。
加 --no-email 则不生成邮箱，email 为空串；默认随机 @qq.com 或 @163.com。
create_time / update_time：约 50% 落在最近 3 天，30% 在 3～10 天前，20% 在 10～30 天前（各段内均匀随机，用户顺序打乱）。
"""

from __future__ import annotations

import argparse
import calendar
import json
import os
import random
import secrets
import sys
import uuid
from dataclasses import dataclass, asdict
from datetime import date, datetime, timedelta, timezone
from typing import List, Optional, Tuple

# GB 11643-1999 公民身份号码校验
_ID_WEIGHTS = (7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
_CHECK_CHARS = "10X98765432"

# 真实县级行政区划代码（示例集，可扩充）
_AREA_CODES = (
    "110101",  # 北京市东城区
    "310101",  # 上海市黄浦区
    "440106",  # 广州市天河区
    "330106",  # 杭州市西湖区
    "320102",  # 南京市玄武区
    "510104",  # 成都市锦江区
)

_SURNAMES = (
    "王",
    "李",
    "张",
    "刘",
    "陈",
    "杨",
    "赵",
    "黄",
    "周",
    "吴",
)
_GIVEN = (
    "伟",
    "芳",
    "娜",
    "敏",
    "静",
    "丽",
    "强",
    "磊",
    "军",
    "洋",
    "艳",
    "勇",
    "杰",
    "涛",
    "明",
    "超",
    "秀英",
    "霞",
    "平",
    "刚",
)


def _random_calendar_date(y0: int, y1: int) -> date:
    y = random.randint(y0, y1)
    m = random.randint(1, 12)
    dmax = calendar.monthrange(y, m)[1]
    d = random.randint(1, dmax)
    return date(y, m, d)


def _checksum_18(first17: str) -> str:
    if len(first17) != 17 or not first17.isdigit():
        raise ValueError("first17 must be 17 digits")
    s = sum(int(first17[i]) * _ID_WEIGHTS[i] for i in range(17))
    return _CHECK_CHARS[s % 11]


def generate_id_no() -> Tuple[str, date, int]:
    """
    返回 (18位身份证号, 出生日期, 性别 1男2女 与 SexEnum 一致)
    第17位：奇数男、偶数女
    """
    area = random.choice(_AREA_CODES)
    birth_d = _random_calendar_date(1970, 2003)
    ymd = birth_d.strftime("%Y%m%d")
    seq_hi = random.randint(0, 98)
    male = random.choice((True, False))
    d17 = random.choice((1, 3, 5, 7, 9)) if male else random.choice((0, 2, 4, 6, 8))
    seq3 = f"{seq_hi:02d}{d17}"
    first17 = area + ymd + seq3
    full = first17 + _checksum_18(first17)
    sex = 1 if male else 2
    return full, birth_d, sex


def _sql_str(s: Optional[str]) -> str:
    if s is None:
        return "NULL"
    return "'" + s.replace("\\", "\\\\").replace("'", "''") + "'"


def _format_mysql_datetime3(d: datetime) -> str:
    """MySQL DATETIME(3) 字面量（毫秒三位）。"""
    ms = d.microsecond // 1000
    return d.strftime("%Y-%m-%d %H:%M:%S") + f".{ms:03d}"


def _uniform_random_datetime(lo: datetime, hi: datetime) -> datetime:
    if hi <= lo:
        return lo
    span = (hi - lo).total_seconds()
    return lo + timedelta(seconds=random.uniform(0, span))


def _time_bucket_counts(n: int) -> Tuple[int, int, int]:
    """按 50% / 30% / 20% 拆人数，余数归最后一个桶；n 很小时就近似。"""
    if n <= 0:
        return (0, 0, 0)
    n_recent = round(n * 0.5)
    n_mid = round(n * 0.3)
    n_old = n - n_recent - n_mid
    while n_old < 0 and (n_recent > 0 or n_mid > 0):
        if n_recent >= n_mid and n_recent > 0:
            n_recent -= 1
        elif n_mid > 0:
            n_mid -= 1
        else:
            n_recent -= 1
        n_old = n - n_recent - n_mid
    return (n_recent, n_mid, n_old)


def _mock_user_for_ledger(u: MockUser) -> dict:
    d = asdict(u)
    ct = d.get("create_time")
    if isinstance(ct, datetime):
        d["create_time"] = ct.isoformat(timespec="milliseconds")
    return d


def assign_staggered_create_times(users: List[MockUser], *, now: Optional[datetime] = None) -> None:
    """
    为每条记录分配 create_time（并用于 update_time）：
    - 桶 0（约 50%）：[now-3d, now]
    - 桶 1（约 30%）：[now-10d, now-3d]
    - 桶 2（约 20%）：[now-30d, now-10d]
    桶与用户随机打乱后再分配；各桶内在区间内均匀随机。
    """
    if not users:
        return
    if now is None:
        now = datetime.now()
    n_recent, n_mid, n_old = _time_bucket_counts(len(users))
    tags = [0] * n_recent + [1] * n_mid + [2] * n_old
    random.shuffle(tags)
    windows = (
        (now - timedelta(days=3), now),
        (now - timedelta(days=10), now - timedelta(days=3)),
        (now - timedelta(days=30), now - timedelta(days=10)),
    )
    for u, tag in zip(users, tags):
        lo, hi = windows[tag]
        ts = _uniform_random_datetime(lo, hi)
        # 对齐到毫秒，避免 SQL 与驱动精度噪声
        u.create_time = ts.replace(microsecond=(ts.microsecond // 1000) * 1000)


def _bcrypt_hash(plain: str) -> str:
    try:
        import bcrypt

        return bcrypt.hashpw(plain.encode("utf-8"), bcrypt.gensalt(rounds=10)).decode("utf-8")
    except ImportError:
        # 无 bcrypt 时仅支持默认口令（bcrypt 2b，Spring BCryptPasswordEncoder 可校验）
        if plain == "123456":
            return "$2b$10$ri/PdFmQxTPT50OW.2GqIe7RPxSVqrXO8wh8d5MOzaW6/4tvWNVji"
        print("未安装 bcrypt 且非默认密码：请 pip install bcrypt", file=sys.stderr)
        sys.exit(1)


@dataclass
class MockUser:
    username: str
    password_hash: str
    nickname: str
    realname: str
    id_no: str
    birth_date: str  # YYYY-MM-DD
    sex: int
    mobile: str
    email: str
    address: str
    create_time: Optional[datetime] = None


def _random_chinese_name() -> str:
    return random.choice(_SURNAMES) + random.choice(_GIVEN)


def _unique_display_name(used: set, max_suffix: int = 9999) -> Optional[str]:
    """生成与已占用集合不重复的姓名串（username / nickname / realname 共用）。"""
    base = _random_chinese_name()
    for n in range(max_suffix + 1):
        cand = base if n == 0 else f"{base}{n}"
        cand = cand[:30]
        if cand not in used:
            return cand
    return None


def _address_for_area(area: str) -> str:
    m = {
        "110101": "北京市东城区东华门街道",
        "310101": "上海市黄浦区南京东路街道",
        "440106": "广州市天河区冼村街道",
        "330106": "杭州市西湖区文新街道",
        "320102": "南京市玄武区新街口街道",
        "510104": "成都市锦江区春熙路街道",
    }
    return m.get(area, "北京市朝阳区建外街道")


_EMAIL_DOMAINS_QQ_163 = ("qq.com", "163.com")


def _alloc_unique_email(used_emails: set) -> str:
    """随机 qq.com（纯数字）或 163.com（字母数字）本地部，批次内唯一，总长不超过 50。"""
    for _ in range(8000):
        domain = random.choice(_EMAIL_DOMAINS_QQ_163)
        if domain == "qq.com":
            local = str(secrets.randbelow(900_000_000) + 100_000_000)  # 9 位数字
        else:
            alphabet = "abcdefghijklmnopqrstuvwxyz0123456789"
            ln = random.randint(9, 14)
            local = "".join(alphabet[secrets.randbelow(len(alphabet))] for _ in range(ln))
        addr = f"{local}@{domain}"
        if len(addr) <= 50 and addr not in used_emails:
            used_emails.add(addr)
            return addr
    raise RuntimeError("无法生成足够多唯一邮箱，请减小 --count")


def build_users(
    count: int,
    mobile_prefix: str,
    password_plain: str,
    *,
    generate_mobile: bool = True,
    generate_email: bool = True,
) -> List[MockUser]:
    used_mobile: set = set()
    used_email: set = set()
    used_username: set = set()
    out: List[MockUser] = []
    pwd_hash = _bcrypt_hash(password_plain)
    suffix_len = 11 - len(mobile_prefix) if generate_mobile else 0
    if generate_mobile:
        if suffix_len < 1:
            raise ValueError("mobile_prefix 长度须小于 11")
    max_attempts = max(count * 200, 1000) if generate_mobile else max(count * 50, 500)
    for _ in range(max_attempts):
        if len(out) >= count:
            break
        id_no, birth_d, sex = generate_id_no()
        display = _unique_display_name(used_username)
        if display is None:
            continue
        area = id_no[:6]
        addr = _address_for_area(area)
        if generate_mobile:
            suffix = secrets.randbelow(10**suffix_len)
            mobile = f"{mobile_prefix}{suffix:0{suffix_len}d}"
            if mobile in used_mobile:
                continue
        else:
            mobile = ""
        if generate_mobile:
            used_mobile.add(mobile)
        used_username.add(display)
        email = _alloc_unique_email(used_email) if generate_email else ""
        out.append(
            MockUser(
                username=display,
                password_hash=pwd_hash,
                nickname=display,
                realname=display,
                id_no=id_no,
                birth_date=birth_d.isoformat(),
                sex=sex,
                mobile=mobile,
                email=email[:50],
                address=addr[:200],
            )
        )
    if len(out) < count:
        hint = "请换 mobile-prefix 或减小 count" if generate_mobile else "请减小 count"
        raise RuntimeError(f"无法在有限尝试内生成 {count} 个用户（{hint}）")
    return out


def _exists_any_conflict_sql(u: MockUser, tenant_id: int, minimal: bool) -> str:
    """
    同租户、未删除行中，任一字段与待插入值相同则视为冲突（不插入）。
    mobile / email 为空串时不参与比较，避免多条空值互相误杀。
    minimal 表无 realname/id_no 列，对应条件省略。
    """
    parts: List[str] = [
        f"e.username = {_sql_str(u.username)}",
        f"e.nickname = {_sql_str(u.nickname)}",
    ]
    if u.email:
        parts.append(f"e.email = {_sql_str(u.email)}")
    if not minimal:
        parts.append(f"e.realname = {_sql_str(u.realname)}")
        parts.append(f"e.id_no = {_sql_str(u.id_no)}")
    if u.mobile:
        parts.append(f"e.mobile = {_sql_str(u.mobile)}")
    or_block = "\n      OR ".join(parts)
    return (
        f"WHERE NOT EXISTS (\n"
        f"  SELECT 1 FROM system_users e\n"
        f"  WHERE e.deleted = 0 AND e.tenant_id = {tenant_id}\n"
        f"    AND (\n      {or_block}\n    )\n"
        f")"
    )


def emit_sql(
    users: List[MockUser],
    tenant_id: int,
    dept_id_sql: str,
    minimal: bool,
) -> str:
    lines = [
        "-- 模拟用户插入：冲突时跳过（不更新、不覆盖）",
        "-- 条件：同 tenant_id 且 deleted=0 下，若 username / nickname / realname / id_no 任一同值，",
        "-- 或非空 mobile / email 与同值，则本行不插入（空 mobile、空 email 不参与比较）。",
        "-- --minimal 时表无 realname、id_no 则不校验这两项。",
        "-- create_time/update_time：约 50% 近 3 天、30% 3～10 天前、20% 10～30 天前（段内均匀随机）。",
        "SET NAMES utf8mb4;",
        "",
    ]
    for u in users:
        remark = "NULL"
        post_ids = "NULL"
        dept = dept_id_sql
        login_ip = "''"
        avatar = "''"
        status = "0"
        creator = _sql_str("1")
        updater = _sql_str("1")
        if u.create_time is not None:
            tsql = _sql_str(_format_mysql_datetime3(u.create_time))
            time_create = tsql
            time_update = tsql
        else:
            time_create = time_update = "NOW(3)"

        base_cols = (
            "username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, "
            "login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id"
        )
        base_vals = (
            f"{_sql_str(u.username)}, {_sql_str(u.password_hash)}, {_sql_str(u.nickname)}, "
            f"{remark}, {dept}, {post_ids}, {_sql_str(u.email)}, {_sql_str(u.mobile)}, {u.sex}, "
            f"{avatar}, {status}, {login_ip}, NULL, {creator}, {time_create}, {updater}, {time_update}, 0, {tenant_id}"
        )

        conflict = _exists_any_conflict_sql(u, tenant_id, minimal)

        if minimal:
            insert_stmt = (
                f"INSERT INTO system_users ({base_cols})\n"
                f"SELECT {base_vals}\n"
                f"FROM DUAL\n"
                f"{conflict};\n"
            )
        else:
            ext_cols = (
                ", verified, id_no, realname, birth_date, address, occupation, education, pay_password"
            )
            ext_vals = (
                f", 0, {_sql_str(u.id_no)}, {_sql_str(u.realname)}, {_sql_str(u.birth_date)}, "
                f"{_sql_str(u.address)}, NULL, NULL, {_sql_str(u.password_hash)}"
            )
            insert_stmt = (
                f"INSERT INTO system_users ({base_cols}{ext_cols})\n"
                f"SELECT {base_vals}{ext_vals}\n"
                f"FROM DUAL\n"
                f"{conflict};\n"
            )
        lines.append(insert_stmt)
        lines.append("")
    return "\n".join(lines)


def main() -> None:
    ap = argparse.ArgumentParser(description="生成 system_users 模拟数据 SQL + 账本 JSON")
    ap.add_argument("--count", type=int, default=10, help="生成用户数")
    ap.add_argument("--tenant-id", type=int, default=1)
    ap.add_argument("--dept-id", type=int, default=None, help="部门 ID，默认 NULL")
    ap.add_argument("--mobile-prefix", type=str, default="199", help="手机号前三位（仅 --no-mobile 未开启时有效）")
    ap.add_argument(
        "--no-mobile",
        dest="generate_mobile",
        action="store_false",
        help="不生成手机号：mobile 写入空串（不触发 idx_mobile_unique）",
    )
    ap.add_argument(
        "--no-email",
        dest="generate_email",
        action="store_false",
        help="不生成邮箱：email 为空串；NOT EXISTS 不比较 email",
    )
    ap.set_defaults(generate_mobile=True, generate_email=True)
    ap.add_argument("--password-plain", type=str, default="123456", help="登录/支付密码明文（写入 bcrypt）")
    ap.add_argument(
        "--minimal",
        action="store_true",
        help="仅基础列（无 id_no/realname 等）；NOT EXISTS 不校验 id_no/realname（旧表无列）",
    )
    ap.add_argument(
        "--out-sql",
        type=str,
        default=None,
        help="SQL 输出路径，默认 script/out/mock_system_users_<batch>.sql",
    )
    ap.add_argument(
        "--ledger",
        type=str,
        default=None,
        help="账本 JSON 路径，默认 script/mock_users_ledger/ledger_<batch>.json",
    )
    args = ap.parse_args()

    if args.generate_mobile:
        if len(args.mobile_prefix) != 3 or not args.mobile_prefix.isdigit():
            print("--mobile-prefix 须为 3 位数字（或加 --no-mobile 跳过手机）", file=sys.stderr)
            sys.exit(2)

    batch_id = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ") + "_" + uuid.uuid4().hex[:8]
    root = os.path.dirname(os.path.abspath(__file__))
    out_sql = args.out_sql or os.path.join(root, "out", f"mock_system_users_{batch_id}.sql")
    ledger_path = args.ledger or os.path.join(root, "mock_users_ledger", f"ledger_{batch_id}.json")

    os.makedirs(os.path.dirname(out_sql), exist_ok=True)
    os.makedirs(os.path.dirname(ledger_path), exist_ok=True)

    dept_sql = str(args.dept_id) if args.dept_id is not None else "NULL"
    users = build_users(
        args.count,
        args.mobile_prefix,
        args.password_plain,
        generate_mobile=args.generate_mobile,
        generate_email=args.generate_email,
    )
    assign_staggered_create_times(users)
    sql_text = emit_sql(users, args.tenant_id, dept_sql, args.minimal)

    with open(out_sql, "w", encoding="utf-8") as f:
        f.write(sql_text)

    ledger = {
        "batch_id": batch_id,
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "purpose": "mock_system_users",
        "tenant_id": args.tenant_id,
        "dept_id": args.dept_id,
        "minimal_columns": args.minimal,
        "generate_mobile": args.generate_mobile,
        "generate_email": args.generate_email,
        "password_plain_note": args.password_plain,
        "sql_file": os.path.relpath(out_sql, root) if out_sql.startswith(root) else out_sql,
        "users": [_mock_user_for_ledger(u) for u in users],
    }
    with open(ledger_path, "w", encoding="utf-8") as f:
        json.dump(ledger, f, ensure_ascii=False, indent=2)

    print(f"Wrote SQL:  {out_sql}")
    print(f"Wrote ledger: {ledger_path}")


if __name__ == "__main__":
    main()
