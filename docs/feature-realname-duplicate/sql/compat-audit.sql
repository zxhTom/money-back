-- 重名兼容改造：上线前只读审计 SQL
-- 说明：本文件所有语句均为只读查询，不修改数据。

-- 1) 用户总量
SELECT COUNT(*) AS total_users FROM system_users;

-- 2) 重名分布（真实姓名）
SELECT realname, COUNT(*) AS cnt
FROM system_users
WHERE deleted = b'0'
  AND realname IS NOT NULL
  AND realname <> ''
GROUP BY realname
HAVING COUNT(*) > 1
ORDER BY cnt DESC, realname
LIMIT 200;

-- 3) 身份证重复（理论上应为 0，若 >0 需先治理再加唯一约束）
SELECT id_no, COUNT(*) AS cnt
FROM system_users
WHERE deleted = b'0'
  AND id_no IS NOT NULL
  AND id_no <> ''
GROUP BY id_no
HAVING COUNT(*) > 1
ORDER BY cnt DESC, id_no
LIMIT 200;

-- 4) 登录名重复（理论上应为 0，若 >0 需先治理再加唯一约束）
SELECT username, COUNT(*) AS cnt
FROM system_users
WHERE deleted = b'0'
  AND username IS NOT NULL
  AND username <> ''
GROUP BY username
HAVING COUNT(*) > 1
ORDER BY cnt DESC, username
LIMIT 200;

-- 5) 手机号重复（仅统计非空）
SELECT mobile, COUNT(*) AS cnt
FROM system_users
WHERE deleted = b'0'
  AND mobile IS NOT NULL
  AND mobile <> ''
GROUP BY mobile
HAVING COUNT(*) > 1
ORDER BY cnt DESC, mobile
LIMIT 200;

-- 6) 合同中同名对应多身份证（借入方）
SELECT indebted_name, COUNT(DISTINCT indebted_id) AS id_variants, COUNT(*) AS cnt
FROM custom_contract
WHERE deleted = b'0'
  AND indebted_name IS NOT NULL
  AND indebted_name <> ''
GROUP BY indebted_name
HAVING COUNT(DISTINCT indebted_id) > 1
ORDER BY id_variants DESC, cnt DESC, indebted_name
LIMIT 200;

-- 7) 合同中同名对应多身份证（出借方）
SELECT creditor_name, COUNT(DISTINCT creditor_id) AS id_variants, COUNT(*) AS cnt
FROM custom_contract
WHERE deleted = b'0'
  AND creditor_name IS NOT NULL
  AND creditor_name <> ''
GROUP BY creditor_name
HAVING COUNT(DISTINCT creditor_id) > 1
ORDER BY id_variants DESC, cnt DESC, creditor_name
LIMIT 200;

-- 8) 小程序绑定唯一键健康检查
SHOW INDEX FROM mini_user;
