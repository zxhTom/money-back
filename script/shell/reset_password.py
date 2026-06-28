#!/usr/bin/env python3
"""
批量重置用户密码并踢出 Token
对齐后台 AdminUserServiceImpl.updateUserPassword() 逻辑

用法:
  python3 reset_password.py passwords.csv
  python3 reset_password.py passwords.csv --dry-run

CSV 格式（UTF-8，第一行标题忽略）:
  username,plain_password
  admin,newpass123
  zhangsan,Abc@2025

依赖: pip install bcrypt pymysql redis
"""

import argparse
import csv
import json
import bcrypt
import pymysql
import redis as redis_lib

# ─── 连接配置（来自 application-tmp.yaml）────────────────────
DB_HOST     = "8.130.191.247"
DB_PORT     = 3306
DB_USER     = "root"
DB_PASSWORD = "Qq_hello_021615996779085"
DB_NAME     = "contract"

REDIS_HOST     = "8.130.191.247"
REDIS_PORT     = 6379
REDIS_PASSWORD = "Qq_hello_021615996779085"
REDIS_DB       = 0
# ─────────────────────────────────────────────────────────────

BCRYPT_ROUNDS   = 4                        # SecurityProperties.passwordEncoderLength = 4
REDIS_TOKEN_KEY = "oauth2_access_token:{}" # RedisKeyConstants.OAUTH2_ACCESS_TOKEN


def hash_password(plain: str) -> str:
    return bcrypt.hashpw(plain.encode(), bcrypt.gensalt(rounds=BCRYPT_ROUNDS)).decode()


def calc_strength(plain: str) -> int:
    """对齐 PasswordStrengthUtil.calc()"""
    if not plain:
        return 1
    score = 0
    if len(plain) >= 8:  score += 1
    if len(plain) >= 12: score += 1
    if any(c.islower() for c in plain):        score += 1
    if any(c.isupper() for c in plain):        score += 1
    if any(c.isdigit() for c in plain):        score += 1
    if any(not c.isalnum() for c in plain):    score += 1
    if score <= 2: return 1
    if score <= 4: return 2
    return 3


def revoke_tokens(db_conn, redis_conn, user_id: int, dry_run: bool) -> int:
    """
    1. DB: UPDATE system_oauth2_access_token SET deleted=1 WHERE user_id=?
    2. DB: UPDATE system_oauth2_refresh_token SET deleted=1 WHERE user_id=?
    3. Redis: SCAN oauth2_access_token:* → 读 value 中 userId → 匹配则 DEL
    """
    # ── DB 软删除 ──────────────────────────────────────────────
    if not dry_run:
        with db_conn.cursor() as cur:
            cur.execute(
                "UPDATE system_oauth2_access_token SET deleted=1 WHERE user_id=%s AND deleted=0",
                (user_id,),
            )
            cur.execute(
                "UPDATE system_oauth2_refresh_token SET deleted=1 WHERE user_id=%s AND deleted=0",
                (user_id,),
            )
        db_conn.commit()

    # ── Redis SCAN 找到属于该用户的所有 token key 并删除 ────────
    deleted_count = 0
    cursor = 0
    while True:
        cursor, keys = redis_conn.scan(cursor, match="oauth2_access_token:*", count=200)
        for key in keys:
            try:
                raw = redis_conn.get(key)
                if not raw:
                    continue
                data = json.loads(raw)
                # Jackson 序列化后字段名为 camelCase
                uid = data.get("userId") or data.get("user_id")
                if uid is None:
                    continue
                if int(uid) == user_id:
                    if not dry_run:
                        redis_conn.delete(key)
                    deleted_count += 1
            except Exception:
                continue
        if cursor == 0:
            break

    return deleted_count


def process(csv_path: str, dry_run: bool):
    tag = "[DRY-RUN] " if dry_run else ""

    db_conn = pymysql.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER,
        password=DB_PASSWORD, database=DB_NAME,
        charset="utf8mb4", autocommit=False,
    )
    redis_conn = redis_lib.Redis(
        host=REDIS_HOST, port=REDIS_PORT,
        password=REDIS_PASSWORD, db=REDIS_DB,
        decode_responses=True,
    )

    with open(csv_path, newline="", encoding="utf-8-sig") as f:
        reader = csv.reader(f)
        next(reader)
        rows = [(r[0].strip(), r[1].strip()) for r in reader if len(r) >= 2 and r[0].strip()]

    ok = fail = 0
    for username, plain in rows:
        with db_conn.cursor() as cur:
            cur.execute(
                "SELECT id FROM system_users WHERE username=%s AND deleted=0 LIMIT 1",
                (username,),
            )
            row = cur.fetchone()

        if not row:
            print(f"  [SKIP]  {username}  ← 用户不存在或已删除")
            fail += 1
            continue

        user_id  = row[0]
        hashed   = hash_password(plain)
        strength = calc_strength(plain)

        if not dry_run:
            with db_conn.cursor() as cur:
                cur.execute(
                    "UPDATE system_users SET password=%s, password_strength=%s WHERE id=%s",
                    (hashed, strength, user_id),
                )
            db_conn.commit()

        kicked = revoke_tokens(db_conn, redis_conn, user_id, dry_run)
        print(f"  {tag}[OK]    {username:<20}  strength={strength}  kicked_tokens={kicked}")
        ok += 1

    print(f"\n完成: 成功={ok}  跳过={fail}")
    db_conn.close()
    redis_conn.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("csv", help="密码 CSV 文件路径")
    parser.add_argument("--dry-run", action="store_true", help="只打印，不写入")
    args = parser.parse_args()
    process(args.csv, args.dry_run)
