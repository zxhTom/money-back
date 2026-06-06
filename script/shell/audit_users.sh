#!/bin/bash
# 用户角色审计 + 密码强度检测 + 可选批量重置密码
# 用法:
#   ./audit_users.sh                          # 只查询审计结果
#   ./audit_users.sh --password <新密码>      # 查询并将问题用户密码统一修改
#
# 排除用户: contractmanager
# 依赖: mysql, python3(bcrypt)

# ── 配置 ──────────────────────────────────────────────────────────────────
MYSQL_HOST="8.130.191.247"
MYSQL_PORT="3306"
MYSQL_DB="contract"
MYSQL_USER="root"
MYSQL_PASS="Qq_hello_021615996779085"

EXCLUDE_USER="contractmanager"
# ─────────────────────────────────────────────────────────────────────────

command -v mysql >/dev/null 2>&1 || { echo "错误: 未找到 mysql 命令" >&2; exit 1; }

MYSQL="mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} --default-character-set=utf8mb4 ${MYSQL_DB}"

# 解析参数
NEW_PASSWORD=""
if [ "$1" = "--password" ]; then
  NEW_PASSWORD="${2:?--password 后需要指定新密码}"
fi

# ── BCrypt 强度评级（从 hash 前缀读取 cost）────────────────────────────────
bcrypt_strength() {
  local hash="$1"
  if [ -z "$hash" ]; then echo "无密码"; return; fi
  # BCrypt 格式: $2b$04$... 按 $ 分割取第3段即 cost
  local cost
  cost=$(echo "$hash" | cut -d'$' -f3)
  if [ -z "$cost" ] || ! [[ "$cost" =~ ^[0-9]+$ ]]; then echo "未知格式"; return; fi
  cost=$((10#$cost))  # 去掉前导零，避免八进制解析问题
  if   [ "$cost" -ge 12 ]; then echo "强 (cost=$cost)"
  elif [ "$cost" -ge 8  ]; then echo "中 (cost=$cost)"
  elif [ "$cost" -ge 4  ]; then echo "弱 (cost=$cost)"
  else                          echo "极弱 (cost=$cost)"
  fi
}

# ── 查询函数 ──────────────────────────────────────────────────────────────
query() { $MYSQL --silent --skip-column-names -e "$1" 2>/dev/null; }

SEP="────────────────────────────────────────────────────────────"

# ══════════════════════════════════════════════════════════════════════════
echo ""
echo "══════════════════ 【一】没有 contract 角色的用户 ══════════════════"
echo "$SEP"
printf "%-6s %-20s %-20s %-24s\n" "ID" "用户名" "昵称" "密码强度"
echo "$SEP"

NO_CONTRACT=$(query "
  SELECT u.id, u.username, u.nickname, COALESCE(u.password, '') AS password
  FROM system_users u
  WHERE u.deleted = 0
    AND u.username != '${EXCLUDE_USER}'
    AND u.id NOT IN (
        SELECT ur.user_id
        FROM system_user_role ur
        JOIN system_role r ON r.id = ur.role_id
        WHERE r.code = 'contract' AND r.deleted = 0 AND ur.deleted = 0
    )
  ORDER BY u.id;
")

NO_CONTRACT_COUNT=0
if [ -n "$NO_CONTRACT" ]; then
  while IFS=$'\t' read -r id username nickname password; do
    strength=$(bcrypt_strength "$password")
    printf "%-6s %-20s %-20s %-24s\n" "$id" "$username" "$nickname" "$strength"
    NO_CONTRACT_COUNT=$((NO_CONTRACT_COUNT + 1))
  done <<< "$NO_CONTRACT"
else
  echo "  （无）"
fi
echo "共 $NO_CONTRACT_COUNT 人"

# ══════════════════════════════════════════════════════════════════════════
echo ""
echo "══════════ 【二】有 contract 角色但同时拥有其他角色的用户 ══════════"
echo "$SEP"
printf "%-6s %-20s %-20s %-10s %-24s\n" "ID" "用户名" "昵称" "角色数" "密码强度"
echo "$SEP"

MULTI_ROLE=$(query "
  SELECT u.id, u.username, u.nickname,
         GROUP_CONCAT(r.name ORDER BY r.id SEPARATOR ',') AS roles,
         COUNT(r.id) AS role_count,
         COALESCE(u.password, '') AS password
  FROM system_users u
  JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = 0
  JOIN system_role r ON r.id = ur.role_id AND r.deleted = 0
  WHERE u.deleted = 0
    AND u.username != '${EXCLUDE_USER}'
    AND u.id IN (
        SELECT ur2.user_id
        FROM system_user_role ur2
        JOIN system_role r2 ON r2.id = ur2.role_id
        WHERE r2.code = 'contract' AND r2.deleted = 0 AND ur2.deleted = 0
    )
  GROUP BY u.id, u.username, u.nickname, u.password
  HAVING COUNT(r.id) > 1
  ORDER BY u.id;
")

MULTI_ROLE_COUNT=0
if [ -n "$MULTI_ROLE" ]; then
  while IFS=$'\t' read -r id username nickname roles role_count password; do
    strength=$(bcrypt_strength "$password")
    printf "%-6s %-20s %-20s %-10s %-24s\n" "$id" "$username" "$nickname" "$role_count" "$strength"
    echo "       角色: $roles"
    MULTI_ROLE_COUNT=$((MULTI_ROLE_COUNT + 1))
  done <<< "$MULTI_ROLE"
else
  echo "  （无）"
fi
echo "共 $MULTI_ROLE_COUNT 人"

# ══════════════════════════════════════════════════════════════════════════
# 如果指定了新密码，执行批量更新
if [ -n "$NEW_PASSWORD" ]; then
  echo ""
  echo "══════════════════ 【三】批量重置密码 ══════════════════"

  # 计算 BCrypt hash
  command -v python3 >/dev/null 2>&1 || { echo "错误: 需要 python3 + bcrypt 模块来生成密码 hash" >&2; exit 1; }
  HASH=$(python3 -c "
import bcrypt, sys
pw = sys.argv[1].encode()
print(bcrypt.hashpw(pw, bcrypt.gensalt(rounds=4)).decode())
" "$NEW_PASSWORD" 2>/dev/null)

  if [ -z "$HASH" ]; then
    echo "错误: BCrypt hash 生成失败，请确认已安装 python3-bcrypt (pip3 install bcrypt)" >&2
    exit 1
  fi

  echo "新密码 hash: $HASH"
  echo ""

  TOTAL=$((NO_CONTRACT_COUNT + MULTI_ROLE_COUNT))
  echo "即将更新 $TOTAL 名用户的密码（排除 ${EXCLUDE_USER}）..."

  $MYSQL -e "
    UPDATE system_users u
    SET u.password    = '${HASH}',
        u.update_time = NOW()
    WHERE u.deleted = 0
      AND u.username != '${EXCLUDE_USER}'
      AND (
          -- 没有 contract 角色
          u.id NOT IN (
              SELECT ur.user_id
              FROM system_user_role ur
              JOIN system_role r ON r.id = ur.role_id
              WHERE r.code = 'contract' AND r.deleted = 0 AND ur.deleted = 0
          )
          OR
          -- 有 contract 但同时有其他角色
          u.id IN (
              SELECT t.uid FROM (
                  SELECT u2.id AS uid
                  FROM system_users u2
                  JOIN system_user_role ur2 ON ur2.user_id = u2.id AND ur2.deleted = 0
                  JOIN system_role r2 ON r2.id = ur2.role_id AND r2.deleted = 0
                  WHERE u2.deleted = 0
                    AND u2.id IN (
                        SELECT ur3.user_id
                        FROM system_user_role ur3
                        JOIN system_role r3 ON r3.id = ur3.role_id
                        WHERE r3.code = 'contract' AND r3.deleted = 0 AND ur3.deleted = 0
                    )
                  GROUP BY u2.id
                  HAVING COUNT(r2.id) > 1
              ) t
          )
      );
  " 2>/dev/null

  echo "完成！已重置 $TOTAL 名用户的密码为指定新密码。"
fi

echo ""
