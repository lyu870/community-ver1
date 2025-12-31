-- notification_setting에 추천 알림 설정 컬럼 추가 (default ON)

SET @schema := DATABASE();

SELECT COUNT(*) INTO @has_post_recommend_enabled
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @schema
  AND TABLE_NAME = 'notification_setting'
  AND COLUMN_NAME = 'post_recommend_enabled';

SET @sql := IF(
  @has_post_recommend_enabled > 0,
  'SELECT 1',
  'ALTER TABLE notification_setting ADD COLUMN post_recommend_enabled TINYINT(1) NOT NULL DEFAULT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
