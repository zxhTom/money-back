#!/bin/bash
# 找出"从未和指定名单出过合同"的用户，按（该用户名下总合同数）降序列出
# 用法:
#   ./find_unrelated_users.sh                       # 只在终端打印前 N 条预览 + 汇总
#   ./find_unrelated_users.sh --export out.csv      # 同时把全量结果导出到 CSV
#   ./find_unrelated_users.sh --preview 50          # 调整终端预览条数（默认20）
#
# 名单口径说明（写死在下面 TARGET_NAMES 里）:
#   - 用户给的原始名单是: 冯书涛,史荣梅,陈积平,张强,刘橙峰,赵志祥,张厚涛,罗意洲,胡花枝,张显
#   - "刘橙峰" 系统里查无此人，最接近的是"刘橙枫"（峰/枫形近字），已确认按"刘橙枫"处理
#   - "张强" 系统里有两个不同身份证号的账号，已确认两个都算作目标名单的一部分
#   - 因此下面的名单是 10 个姓名对应的 11 个身份证号（张强算2个）
#
# 判定规则:
#   - "和名单出过合同" = custom_contract 里任意一条未删除记录，
#     该用户的身份证号出现在 indebted_id/creditor_id 一侧，名单中任意一人的身份证号出现在另一侧
#   - "剩下的人"排除: 名单本身这些人、没有身份证号的账号（无法判定关系）
#   - 合同数量 = 该用户名下（不限交易对象）的全部未删除合同总数，不是"和名单的合同数"
#     （和名单的合同数按定义就是0，列出来没有意义，所以这里给的是总活跃度参考）
#
# 依赖: mysql

# ── 配置 ──────────────────────────────────────────────────────────────────
MYSQL_HOST="8.130.191.247"
MYSQL_PORT="3306"
MYSQL_DB="contract"
MYSQL_USER="root"
MYSQL_PASS="Qq_hello_021615996779085"
# 孙立豪 朋友
TARGET_NAMES="'张新华','姜麟峰','姚直东','zxhtom','风速1','杨佳洁','叶凌宇','凌建刚','陈永章','孙立豪','风速合约','冯书涛','史荣梅','陈积平','张强','刘橙枫','赵志祥','张厚涛','罗意洲','胡花枝','张显'"
# ─────────────────────────────────────────────────────────────────────────

command -v mysql >/dev/null 2>&1 || { echo "错误: 未找到 mysql 命令" >&2; exit 1; }

MYSQL="mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} --default-character-set=utf8mb4 ${MYSQL_DB}"
query() { $MYSQL --silent --skip-column-names -e "$1" 2>/dev/null; }

# ── 解析参数 ──────────────────────────────────────────────────────────────
EXPORT_FILE=""
PREVIEW_COUNT=20

while [ $# -gt 0 ]; do
  case "$1" in
    --export)
      EXPORT_FILE="${2:?--export 后需要指定输出文件路径}"
      shift 2
      ;;
    --preview)
      PREVIEW_COUNT="${2:?--preview 后需要指定预览条数}"
      shift 2
      ;;
    *)
      echo "未知参数: $1" >&2
      echo "用法: $0 [--export <文件路径>] [--preview <条数>]" >&2
      exit 1
      ;;
  esac
done

SEP="──────────────────────────────────────────────────────────────────────"

# ══════════════════════════════════════════════════════════════════════════
echo "══════════════════ 【一】名单解析结果 ══════════════════"
echo "$SEP"
printf "%-10s %-20s\n" "姓名" "身份证号"
echo "$SEP"
query "
  SELECT realname, id_no FROM system_users
  WHERE deleted = 0 AND realname IN (${TARGET_NAMES}) AND id_no IS NOT NULL AND id_no <> ''
  ORDER BY realname;
" | while IFS=$'\t' read -r realname id_no; do
  printf "%-10s %-20s\n" "$realname" "$id_no"
done

# ══════════════════════════════════════════════════════════════════════════
FULL_SQL="
WITH target_ids AS (
  SELECT id_no FROM system_users
  WHERE deleted = 0 AND realname IN (${TARGET_NAMES}) AND id_no IS NOT NULL AND id_no <> ''
)
SELECT u.username, u.id_no,
  (SELECT COUNT(*) FROM custom_contract c
   WHERE c.deleted = 0 AND (c.indebted_id = u.id_no OR c.creditor_id = u.id_no)) AS contract_count
FROM system_users u
WHERE u.deleted = 0
  AND u.id_no IS NOT NULL AND u.id_no <> ''
  AND u.id_no NOT IN (SELECT id_no FROM target_ids)
  AND NOT EXISTS (
    SELECT 1 FROM custom_contract c
    WHERE c.deleted = 0
      AND (
        (c.indebted_id = u.id_no AND c.creditor_id IN (SELECT id_no FROM target_ids))
        OR
        (c.creditor_id = u.id_no AND c.indebted_id IN (SELECT id_no FROM target_ids))
      )
  )
ORDER BY contract_count DESC, u.username
"

TOTAL=$(query "SELECT COUNT(*) FROM system_users WHERE deleted=0;")
LINKED=$(query "
  WITH target_ids AS (
    SELECT id_no FROM system_users
    WHERE deleted = 0 AND realname IN (${TARGET_NAMES}) AND id_no IS NOT NULL AND id_no <> ''
  )
  SELECT COUNT(DISTINCT u.id_no) FROM system_users u
  WHERE u.deleted = 0 AND u.id_no IS NOT NULL AND u.id_no <> ''
    AND u.id_no NOT IN (SELECT id_no FROM target_ids)
    AND EXISTS (
      SELECT 1 FROM custom_contract c
      WHERE c.deleted = 0
        AND (
          (c.indebted_id = u.id_no AND c.creditor_id IN (SELECT id_no FROM target_ids))
          OR
          (c.creditor_id = u.id_no AND c.indebted_id IN (SELECT id_no FROM target_ids))
        )
    );
")

echo ""
echo "══════════════════ 【二】和名单出过合同的用户数 ══════════════════"
echo "$SEP"
echo "全库有效用户: $TOTAL 人；名单本身: $(query "SELECT COUNT(*) FROM system_users WHERE deleted=0 AND realname IN (${TARGET_NAMES});") 人；和名单出过合同的其他用户: $LINKED 人"

# ══════════════════════════════════════════════════════════════════════════
echo ""
echo "══════════════ 【三】完全没和名单出过合同的用户（预览前 ${PREVIEW_COUNT} 条，按合同数降序）══════════════"
echo "$SEP"
printf "%-20s %-20s %-10s\n" "用户名" "身份证号" "合同数量"
echo "$SEP"

query "${FULL_SQL} LIMIT ${PREVIEW_COUNT};" | while IFS=$'\t' read -r username id_no contract_count; do
  printf "%-20s %-20s %-10s\n" "$username" "$id_no" "$contract_count"
done

REMAIN_TOTAL=$(query "
  WITH target_ids AS (
    SELECT id_no FROM system_users
    WHERE deleted = 0 AND realname IN (${TARGET_NAMES}) AND id_no IS NOT NULL AND id_no <> ''
  )
  SELECT COUNT(*) FROM system_users u
  WHERE u.deleted = 0
    AND u.id_no IS NOT NULL AND u.id_no <> ''
    AND u.id_no NOT IN (SELECT id_no FROM target_ids)
    AND NOT EXISTS (
      SELECT 1 FROM custom_contract c
      WHERE c.deleted = 0
        AND (
          (c.indebted_id = u.id_no AND c.creditor_id IN (SELECT id_no FROM target_ids))
          OR
          (c.creditor_id = u.id_no AND c.indebted_id IN (SELECT id_no FROM target_ids))
        )
    );
")
echo "$SEP"
echo "剩余名单共 $REMAIN_TOTAL 人（终端只预览了前 ${PREVIEW_COUNT} 条，全量请用 --export 导出）"

# ══════════════════════════════════════════════════════════════════════════
if [ -n "$EXPORT_FILE" ]; then
  {
    echo "用户名,身份证,合同数量"
    query "${FULL_SQL};" | while IFS=$'\t' read -r username id_no contract_count; do
      echo "\"${username}\",\"${id_no}\",${contract_count}"
    done
  } > "$EXPORT_FILE"
  echo ""
  echo "已导出全量 $REMAIN_TOTAL 条记录到: $EXPORT_FILE"
fi

echo ""
