-- V7__create_notification_dlt.sql
CREATE TABLE IF NOT EXISTS notification_dlt (
    id BIGINT NOT NULL AUTO_INCREMENT,

    dlt_topic VARCHAR(200) NOT NULL,
    dlt_partition INT NOT NULL,
    dlt_offset BIGINT NOT NULL,

    original_topic VARCHAR(200) NULL,
    original_partition INT NULL,
    original_offset BIGINT NULL,

    record_key VARCHAR(200) NULL,

    event_id VARCHAR(200) NULL,
    type VARCHAR(50) NULL,
    target_member_id BIGINT NULL,

    payload_json LONGTEXT NULL,

    exception_fqcn VARCHAR(300) NULL,
    exception_message VARCHAR(1000) NULL,
    exception_stacktrace LONGTEXT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_dlt_record (dlt_topic, dlt_partition, dlt_offset)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
