#!/bin/bash
# 删除 token（Redis + MySQL 同时清除）
# 用法:
#   ./delete_user_tokens.sh --all                # 删除所有用户的全部 token
#   ./delete_user_tokens.sh <username>           # 删除指定用户名的 token
#   ./delete_user_tokens.sh --id <userId>        # 删除指定 userId 的 token
#
# 依赖: redis-cli, mysql

set -e

# ── 配置 ──────────────────────────────────────────────────────────────────
REDIS_HOST="8.130.191.247"
REDIS_PORT="6379"
REDIS_DB="0"
REDIS_PASS="Qq_hello_021615996779085"

MYSQL_HOST="8.130.191.247"
MYSQL_PORT="3306"
MYSQL_DB="contract"
MYSQL_USER="root"
MYSQL_PASS="Qq_hello_021615996779085"
# ─────────────────────────────────────────────────────────────────────────

for cmd in redis-cli mysql; do
  command -v "$cmd" >/dev/null 2>&1 || { echo "错误: 未找到命令 '$cmd'" >&2; exit 1; }
done

MYSQL_ARGS=(-h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASS" --silent --skip-column-names "$MYSQL_DB")
REDIS_ARGS=(-h "$REDIS_HOST" -p "$REDIS_PORT" -n "$REDIS_DB" -a "$REDIS_PASS" --no-auth-warning)

mysql_exec() { mysql "${MYSQL_ARGS[@]}" -e "$1" 2>/dev/null; }

# ── 解析参数，确定 WHERE 条件 ─────────────────────────────────────────────
if [ "$1" = "--all" ]; then
  LABEL="所有用户"
  WHERE_CLAUSE=""          # 无条件，删全部
elif [ "$1" = "--id" ]; then
  USER_ID="${2:?用法: $0 --id <userId>}"
  LABEL="userId=$USER_ID"
  WHERE_CLAUSE="WHERE user_id = ${USER_ID} AND deleted = 0"
else
  USERNAME="${1:?用法: $0 --all | <username> | --id <userId>}"
  USER_ID=$(mysql_exec "SELECT id FROM system_user WHERE username = '${USERNAME}' AND deleted = 0 LIMIT 1;")
  if [ -z "$USER_ID" ]; then
    echo "错误: 未找到用户名 '$USERNAME'" >&2; exit 1
  fi
  echo "用户 '$USERNAME' → userId=$USER_ID"
  LABEL="用户 $USERNAME (userId=$USER_ID)"
  WHERE_CLAUSE="WHERE user_id = ${USER_ID} AND deleted = 0"
fi

ALL_WHERE="${WHERE_CLAUSE:-WHERE deleted = 0}"

# ── 第一步：从 MySQL 拿到 token 列表（用于精准删 Redis）─────────────────
echo ""
echo "[1/2] 清除 MySQL ..."

MYSQL_TOKENS=$(mysql_exec "SELECT access_token FROM system_oauth2_access_token ${ALL_WHERE};")

AT_COUNT=$(mysql_exec "SELECT COUNT(*) FROM system_oauth2_access_token ${ALL_WHERE};")
mysql_exec "UPDATE system_oauth2_access_token SET deleted = 1 ${ALL_WHERE};"
echo "  access_token 表：已标记删除 $AT_COUNT 条"

RT_COUNT=$(mysql_exec "SELECT COUNT(*) FROM system_oauth2_refresh_token ${ALL_WHERE};")
mysql_exec "UPDATE system_oauth2_refresh_token SET deleted = 1 ${ALL_WHERE};"
echo "  refresh_token 表：已标记删除 $RT_COUNT 条"

# ── 第二步：删 Redis ──────────────────────────────────────────────────────
echo ""
echo "[2/2] 清除 Redis ..."
REDIS_DELETED=0

if [ -n "$MYSQL_TOKENS" ]; then
  while IFS= read -r TOKEN; do
    [ -z "$TOKEN" ] && continue
    redis-cli "${REDIS_ARGS[@]}" DEL "oauth2_access_token:${TOKEN}" >/dev/null
    REDIS_DELETED=$((REDIS_DELETED + 1))
  done <<< "$MYSQL_TOKENS"
  echo "  按 MySQL token 列表精准删除: $REDIS_DELETED 个"
fi

# 兜底扫描，清理 MySQL 里已物理删除但 Redis 仍存在的 key
CURSOR=0; SCAN_DELETED=0
while true; do
  RESULT=$(redis-cli "${REDIS_ARGS[@]}" SCAN "$CURSOR" MATCH "oauth2_access_token:*" COUNT 200)
  CURSOR=$(echo "$RESULT" | head -1)
  KEYS=$(echo "$RESULT" | tail -n +2)
  if [ -n "$KEYS" ]; then
    while IFS= read -r KEY; do
      [ -z "$KEY" ] && continue
      redis-cli "${REDIS_ARGS[@]}" DEL "$KEY" >/dev/null
      SCAN_DELETED=$((SCAN_DELETED + 1))
    done <<< "$KEYS"
  fi
  [ "$CURSOR" = "0" ] && break
done
[ "$SCAN_DELETED" -gt 0 ] && echo "  兜底扫描额外删除: $SCAN_DELETED 个"

echo ""
echo "完成！对象: $LABEL"
echo "  MySQL access_token  删除: $AT_COUNT 条"
echo "  MySQL refresh_token 删除: $RT_COUNT 条"
echo "  Redis key 共删除: $((REDIS_DELETED + SCAN_DELETED)) 个"
