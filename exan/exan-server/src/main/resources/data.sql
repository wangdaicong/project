INSERT INTO edu_stage(`code`,`name`,`status`,`sort`) VALUES
('primary','小学',1,1),
('junior','初中',1,2),
('senior','高中',1,3)
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status), sort=VALUES(sort);

INSERT INTO subject(`stage_id`,`code`,`name`,`status`,`sort`)
SELECT s.id,'math','数学',1,1 FROM edu_stage s WHERE s.code='primary'
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status), sort=VALUES(sort);

INSERT INTO subject(`stage_id`,`code`,`name`,`status`,`sort`)
SELECT s.id,'chinese','语文',1,2 FROM edu_stage s WHERE s.code='primary'
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status), sort=VALUES(sort);

INSERT INTO subject(`stage_id`,`code`,`name`,`status`,`sort`)
SELECT s.id,'english','英语',1,3 FROM edu_stage s WHERE s.code='primary'
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status), sort=VALUES(sort);

INSERT INTO sys_user(`username`,`password_hash`,`display_name`,`role`,`status`)
VALUES('demo',NULL,'演示用户','STUDENT',1)
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), role=VALUES(role), status=VALUES(status);

INSERT INTO paper(`stage_id`,`subject_id`,`name`,`paper_type`,`total_score`,`time_limit_sec`,`version`,`status`,`pricing_type`,`price_cent`,`paper_date`,`region_code`)
SELECT s.stage_id, s.id,
       '2026-01-01 小学数学 模拟卷',
       'FIXED', 100, 3600, 1, 'ONLINE', 'FREE', 0, '2026-01-01', 'CN'
FROM subject s
JOIN edu_stage es ON es.id = s.stage_id
WHERE es.code='primary' AND s.code='math'
ON DUPLICATE KEY UPDATE status=VALUES(status);

INSERT INTO paper(`stage_id`,`subject_id`,`name`,`paper_type`,`total_score`,`time_limit_sec`,`version`,`status`,`pricing_type`,`price_cent`,`paper_date`,`region_code`)
SELECT s.stage_id, s.id,
       '2025-12-15 小学数学 模拟卷',
       'FIXED', 100, 3600, 1, 'ONLINE', 'FREE', 0, '2025-12-15', 'CN'
FROM subject s
JOIN edu_stage es ON es.id = s.stage_id
WHERE es.code='primary' AND s.code='math'
ON DUPLICATE KEY UPDATE status=VALUES(status);
