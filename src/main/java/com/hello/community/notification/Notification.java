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
    }

    public static Notification of(Member member, NotificationType type, String message, String linkUrl, String eventId) {
        Notification n = new Notification();
        n.member = member;
        n.type = type;
        n.message = message;
        n.linkUrl = linkUrl;
        n.eventId = eventId;
        n.createdAt = LocalDateTime.now();
        return n;
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
