-- 이미 message 있으면 아무것도 안함
-- content가 있으면 message로 컬럼명 변경 (타입은 content타입 그대로유지)
-- 둘다없으면 message VARCHAR(255) 추가

SET @schema := DATABASE();

SELECT COUNT(*) INTO @has_message
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @schema
  AND TABLE_NAME = 'notification'
  AND COLUMN_NAME = 'message';

SELECT COUNT(*) INTO @has_content
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @schema
  AND TABLE_NAME = 'notification'
  AND COLUMN_NAME = 'content';

SELECT COLUMN_TYPE INTO @content_type
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @schema
  AND TABLE_NAME = 'notification'
  AND COLUMN_NAME = 'content'
LIMIT 1;

SET @sql := IF(
  @has_message > 0,
  'SELECT 1',
  IF(
    @has_content > 0,
    CONCAT('ALTER TABLE notification CHANGE COLUMN content message ', @content_type, ' NULL'),
    'ALTER TABLE notification ADD COLUMN message VARCHAR(255) NULL'
  )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
