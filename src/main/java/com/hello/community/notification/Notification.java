// Notification.java
package com.hello.community.notification;

import com.hello.community.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification",
        indexes = {
                @Index(name = "idx_notification_member_created", columnList = "member_id, created_at"),
                @Index(name = "idx_notification_member_read", columnList = "member_id, read_at")
        }
)
@Getter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "event_id", length = 200)
    private String eventId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.title == null || this.title.trim().isEmpty()) {
            this.title = defaultTitle(this.type);
        }
    }

    public static Notification of(Member member, NotificationType type, String title, String message, String linkUrl, String eventId) {
        Notification n = new Notification();
        n.member = member;
        n.type = type;
        n.title = title;
        n.message = message;
        n.linkUrl = linkUrl;
        n.eventId = eventId;
        n.createdAt = LocalDateTime.now();
        return n;
    }

    public static Notification of(Member member, NotificationType type, String message, String linkUrl, String eventId) {
        return of(member, type, defaultTitle(type), message, linkUrl, eventId);
    }

    private static String defaultTitle(NotificationType type) {
        if (type == null) {
            return "알림";
        }

        if (type == NotificationType.POST_COMMENT) {
            return "내 게시글 댓글";
        }

        if (type == NotificationType.COMMENT_REPLY) {
            return "내 댓글 답글";
        }

        if (type == NotificationType.BOARD_POST) {
            return "새 글 알림";
        }

        return "알림";
    }

    public boolean isRead() {
        return this.readAt != null;
    }

    public void markRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }
}
