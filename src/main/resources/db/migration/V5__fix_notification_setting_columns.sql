-- V5__fix_notification_setting_columns.sql
-- notification_setting 컬럼명 정리:
-- my_post_comment_enabled -> post_comment_enabled
-- my_comment_reply_enabled -> comment_reply_enabled
-- 이미 있으면 스킵, 없으면 rename 또는 add

SET @schema := DATABASE();

-- post_comment_enabled 처리
SELECT COUNT(*) INTO @has_post_comment_enabled
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @schema
  AND TABLE_NAME = 'notification_setting'
  AND COLUMN_NAME = 'post_comment_enabled';

SELECT COUNT(*) INTO @has_my_post_comment_enabled
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @schema
  AND TABLE_NAME = 'notification_setting'
  AND COLUMN_NAME = 'my_post_comment_enabled';

SET @sql := IF(
  @has_post_comment_enabled > 0,
  'SELECT 1',
  IF(
    @has_my_post_comment_enabled > 0,
    'ALTER TABLE notification_setting CHANGE COLUMN my_post_comment_enabled post_comment_enabled TINYINT(1) NOT NULL DEFAULT 1',
    'ALTER TABLE notification_setting ADD COLUMN post_comment_enabled TINYINT(1) NOT NULL DEFAULT 1'
  )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- comment_reply_enabled 처리
SELECT COUNT(*) INTO @has_comment_reply_enabled
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @schema
  AND TABLE_NAME = 'notification_setting'
  AND COLUMN_NAME = 'comment_reply_enabled';

SELECT COUNT(*) INTO @has_my_comment_reply_enabled
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @schema
  AND TABLE_NAME = 'notification_setting'
  AND COLUMN_NAME = 'my_comment_reply_enabled';

SET @sql := IF(
  @has_comment_reply_enabled > 0,
  'SELECT 1',
  IF(
    @has_my_comment_reply_enabled > 0,
    'ALTER TABLE notification_setting CHANGE COLUMN my_comment_reply_enabled comment_reply_enabled TINYINT(1) NOT NULL DEFAULT 1',
    'ALTER TABLE notification_setting ADD COLUMN comment_reply_enabled TINYINT(1) NOT NULL DEFAULT 1'
  )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
