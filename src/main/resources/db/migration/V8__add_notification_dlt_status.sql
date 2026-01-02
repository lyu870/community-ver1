-- V8__add_notification_dlt_status.sql
ALTER TABLE notification_dlt
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN handled_at DATETIME NULL;