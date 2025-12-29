-- 알림 기능 테이블 추가 (notification / notification_setting / board_subscription)

CREATE TABLE `notification_setting` (
  `member_id` bigint NOT NULL,
  `my_post_comment_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `my_comment_reply_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`member_id`),
  CONSTRAINT `FK_notification_setting_member`
    FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `board_subscription` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `board_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_board_subscription_member_board` (`member_id`, `board_type`),
  KEY `idx_board_subscription_member` (`member_id`),
  CONSTRAINT `FK_board_subscription_member`
    FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `type` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `body` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `link_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `read_at` datetime(6) DEFAULT NULL,
  `event_id` char(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_member_created` (`member_id`, `created_at`),
  KEY `idx_notification_member_read_created` (`member_id`, `is_read`, `created_at`),
  UNIQUE KEY `uk_notification_member_event` (`member_id`, `event_id`),
  CONSTRAINT `FK_notification_member`
    FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;
