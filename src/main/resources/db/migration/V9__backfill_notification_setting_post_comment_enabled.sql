-- V9__backfill_notification_setting_post_comment_enabled.sql
-- post_comment_enabled가 0인데 사용자가 설정을 실제로 수정한 적이 없는 레코드만 1로 복구.

UPDATE notification_setting
SET post_comment_enabled = 1
WHERE post_comment_enabled = 0
  AND (updated_at IS NULL OR updated_at = created_at);