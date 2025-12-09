CREATE UNIQUE INDEX idx_mobile_unique
ON system_users (
     mobile,
    (CASE WHEN deleted = 0 AND mobile IS NOT null AND mobile!='' THEN 0 ELSE NULL END)
    );