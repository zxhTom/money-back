#!/bin/bash
# 快速获取指定用户的 accessToken
# 用法:
#   ./get_token.sh <username> <password> [idNo] [tenant_id] [base_url]
# 示例:
#   ./get_token.sh admin admin123
#   ./get_token.sh zhangsan pass123 110101199001011234
#   ./get_token.sh zhangsan pass123 "" 2 http://127.0.0.1:48080

set -e

USERNAME="${1:?用法: $0 <username> <password> [idNo] [tenant_id] [base_url]}"
PASSWORD="${2:?用法: $0 <username> <password> [idNo] [tenant_id] [base_url]}"
ID_NO="${3:-}"
TENANT_ID="${4:-1}"
BASE_URL="${5:-http://127.0.0.1:48080}"

# 构造请求体
if [ -n "$ID_NO" ]; then
  BODY=$(printf '{"username":"%s","password":"%s","idNo":"%s"}' "$USERNAME" "$PASSWORD" "$ID_NO")
else
  BODY=$(printf '{"username":"%s","password":"%s"}' "$USERNAME" "$PASSWORD")
fi

RESPONSE=$(curl -s -X POST "$BASE_URL/system/auth/login" \
  -H "Content-Type: application/json" \
  -H "tenant-id: $TENANT_ID" \
  -d "$BODY")

# 检查是否有错误
CODE=$(echo "$RESPONSE" | grep -o '"code":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ "$CODE" != "0" ]; then
  MSG=$(echo "$RESPONSE" | grep -o '"msg":"[^"]*"' | head -1 | sed 's/"msg":"//;s/"//')
  echo "登录失败 (code=$CODE): $MSG" >&2
  echo "完整响应: $RESPONSE" >&2
  exit 1
fi

TOKEN=$(echo "$RESPONSE" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//;s/"//')
if [ -z "$TOKEN" ]; then
  echo "未能解析 accessToken，完整响应:" >&2
  echo "$RESPONSE" >&2
  exit 1
fi

echo "$TOKEN"
