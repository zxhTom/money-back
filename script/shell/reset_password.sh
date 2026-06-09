#!/bin/bash
# 批量修改用户密码（改密后同步踢出所有在线 token）
# 用法:
#   ./reset_password.sh -f users.txt                   # 文件: 每行 "用户名" 或 "用户名,密码"
#   ./reset_password.sh -f users.txt -p <新密码>       # 文件提供用户名，统一用指定密码
#   ./reset_password.sh -u <用户名> -p <新密码>        # 单个用户
#
# 未指定密码时使用默认密码: $Qq15996779085
# 依赖: mysql, redis-cli, python3(bcrypt)

# ── 配置 ──────────────────────────────────────────────────────────────────
MYSQL_HOST="8.130.191.247"
MYSQL_PORT="3306"
MYSQL_DB="contract"
MYSQL_USER="root"
MYSQL_PASS="Qq_hello_021615996779085"

REDIS_HOST="8.130.191.247"
REDIS_PORT="6379"
REDIS_DB="0"
REDIS_PASS="Qq_hello_021615996779085"

DEFAULT_PASSWORD='$Qq15996779085'
# ─────────────────────────────────────────────────────────────────────────

command -v mysql     >/dev/null 2>&1 || { echo "错误: 未找到 mysql 命令" >&2; exit 1; }
command -v redis-cli >/dev/null 2>&1 || { echo "错误: 未找到 redis-cli 命令" >&2; exit 1; }
command -v python3   >/dev/null 2>&1 || { echo "错误: 需要 python3 + bcrypt 模块" >&2; exit 1; }

REDIS_ARGS=(-h "$REDIS_HOST" -p "$REDIS_PORT" -n "$REDIS_DB" -a "$REDIS_PASS" --no-auth-warning)

MYSQL_CMD="mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} --default-character-set=utf8mb4 ${MYSQL_DB}"

query() { $MYSQL_CMD --silent --skip-column-names -e "$1" 2>/dev/null; }

bcrypt_hash() {
  python3 -c "
import bcrypt, sys
pw = sys.argv[1].encode()
print(bcrypt.hashpw(pw, bcrypt.gensalt(rounds=4)).decode())
" "$1" 2>/dev/null
}

# ── 参数解析 ──────────────────────────────────────────────────────────────
FILE=""
CLI_PASSWORD=""
SINGLE_USER=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    -f) FILE="${2:?-f 后需要指定文件路径}"; shift 2 ;;
    -p) CLI_PASSWORD="${2:?-p 后需要指定密码}"; shift 2 ;;
    -u) SINGLE_USER="${2:?-u 后需要指定用户名}"; shift 2 ;;
    *)  echo "未知参数: $1" >&2; exit 1 ;;
  esac
done

if [ -z "$FILE" ] && [ -z "$SINGLE_USER" ]; then
  echo "错误: 必须通过 -f <文件> 或 -u <用户名> 指定用户" >&2
  exit 1
fi

if [ -n "$FILE" ] && [ -n "$SINGLE_USER" ]; then
  echo "错误: -f 和 -u 不能同时使用" >&2
  exit 1
fi

if [ -n "$FILE" ] && [ ! -f "$FILE" ]; then
  echo "错误: 文件不存在: $FILE" >&2
  exit 1
fi

# ── 构建 (username, password) 列表 ────────────────────────────────────────
declare -a USERNAMES
declare -a PASSWORDS

if [ -n "$SINGLE_USER" ]; then
  USERNAMES+=("$SINGLE_USER")
  PASSWORDS+=("${CLI_PASSWORD:-$DEFAULT_PASSWORD}")
else
  while IFS= read -r line || [ -n "$line" ]; do
    # 跳过空行和注释
    [[ -z "$line" || "$line" == \#* ]] && continue

    if [[ "$line" == *,* ]]; then
      username="${line%%,*}"
      file_password="${line#*,}"
      username="${username// /}"
      file_password="${file_password// /}"
    else
      username="${line// /}"
      file_password=""
    fi

    if [ -z "$username" ]; then
      echo "警告: 跳过空用户名行: $line" >&2
      continue
    fi

    # 优先级: 命令行 -p > 文件内密码 > 默认密码
    if [ -n "$CLI_PASSWORD" ]; then
      password="$CLI_PASSWORD"
    elif [ -n "$file_password" ]; then
      password="$file_password"
    else
      password="$DEFAULT_PASSWORD"
    fi

    USERNAMES+=("$username")
    PASSWORDS+=("$password")
  done < "$FILE"
fi

if [ ${#USERNAMES[@]} -eq 0 ]; then
  echo "错误: 没有找到任何有效用户" >&2
  exit 1
fi

# ── 执行更新 ──────────────────────────────────────────────────────────────
echo ""
echo "══════════════════ 批量修改密码 ══════════════════"
printf "%-20s %-10s %s\n" "用户名" "状态" "说明"
echo "────────────────────────────────────────────────"

SUCCESS=0
FAILED=0

for i in "${!USERNAMES[@]}"; do
  username="${USERNAMES[$i]}"
  password="${PASSWORDS[$i]}"

  # 验证用户存在
  user_id=$(query "SELECT id FROM system_users WHERE username='${username}' AND deleted=0 LIMIT 1;")
  if [ -z "$user_id" ]; then
    printf "%-20s %-10s %s\n" "$username" "跳过" "用户不存在"
    FAILED=$((FAILED + 1))
    continue
  fi

  # 生成 bcrypt hash
  hash=$(bcrypt_hash "$password")
  if [ -z "$hash" ]; then
    printf "%-20s %-10s %s\n" "$username" "失败" "hash 生成失败，请确认已安装 python3-bcrypt"
    FAILED=$((FAILED + 1))
    continue
  fi

  # 更新密码
  $MYSQL_CMD -e "
    UPDATE system_users
    SET password='${hash}', update_time=NOW()
    WHERE id=${user_id} AND deleted=0;
  " 2>/dev/null

  if [ $? -ne 0 ]; then
    printf "%-20s %-10s %s\n" "$username" "失败" "SQL 执行出错"
    FAILED=$((FAILED + 1))
    continue
  fi

  # 踢出所有在线 token（MySQL 软删除 + Redis 删 key）
  tokens=$(query "SELECT access_token FROM system_oauth2_access_token WHERE user_id=${user_id} AND deleted=0;")
  query "UPDATE system_oauth2_access_token SET deleted=1 WHERE user_id=${user_id} AND deleted=0;"
  query "UPDATE system_oauth2_refresh_token SET deleted=1 WHERE user_id=${user_id} AND deleted=0;"
  token_count=0
  if [ -n "$tokens" ]; then
    while IFS= read -r tok; do
      [ -z "$tok" ] && continue
      redis-cli "${REDIS_ARGS[@]}" DEL "oauth2_access_token:${tok}" >/dev/null
      token_count=$((token_count + 1))
    done <<< "$tokens"
  fi

  printf "%-20s %-10s %s\n" "$username" "成功" "id=${user_id}，踢出 token ${token_count} 个"
  SUCCESS=$((SUCCESS + 1))
done

echo "────────────────────────────────────────────────"
echo "完成: 成功 ${SUCCESS} 人，跳过/失败 ${FAILED} 人"
echo ""
