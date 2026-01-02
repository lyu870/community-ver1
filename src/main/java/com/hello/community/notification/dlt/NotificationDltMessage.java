// NotificationDltMessage.java
package com.hello.community.notification.dlt;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_dlt")
@Getter
@NoArgsConstructor
public class NotificationDltMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dlt_topic", nullable = false, length = 200)
    private String dltTopic;

    @Column(name = "dlt_partition", nullable = false)
    private Integer dltPartition;

    @Column(name = "dlt_offset", nullable = false)
    private Long dltOffset;

    @Column(name = "original_topic", length = 200)
    private String originalTopic;

    @Column(name = "original_partition")
    private Integer originalPartition;

    @Column(name = "original_offset")
    private Long originalOffset;

    @Column(name = "record_key", length = 200)
    private String recordKey;

    @Column(name = "event_id", length = 200)
    private String eventId;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "target_member_id")
    private Long targetMemberId;

    @Lob
    @Column(name = "payload_json", columnDefinition = "longtext")
    private String payloadJson;

    @Column(name = "exception_fqcn", length = 300)
    private String exceptionFqcn;

    @Column(name = "exception_message", length = 1000)
    private String exceptionMessage;

    @Lob
    @Column(name = "exception_stacktrace", columnDefinition = "longtext")
    private String exceptionStacktrace;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationDltStatus status;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static NotificationDltMessage of(String dltTopic,
                                            Integer dltPartition,
                                            Long dltOffset,
                                            String originalTopic,
                                            Integer originalPartition,
                                            Long originalOffset,
                                            String recordKey,
                                            String eventId,
                                            String type,
                                            Long targetMemberId,
                                            String payloadJson,
                                            String exceptionFqcn,
                                            String exceptionMessage,
                                            String exceptionStacktrace) {

        NotificationDltMessage m = new NotificationDltMessage();
        m.dltTopic = dltTopic;
        m.dltPartition = dltPartition;
        m.dltOffset = dltOffset;
        m.originalTopic = originalTopic;
        m.originalPartition = originalPartition;
        m.originalOffset = originalOffset;
        m.recordKey = recordKey;
        m.eventId = eventId;
        m.type = type;
        m.targetMemberId = targetMemberId;
        m.payloadJson = payloadJson;
        m.exceptionFqcn = exceptionFqcn;
        m.exceptionMessage = exceptionMessage;
        m.exceptionStacktrace = exceptionStacktrace;
        return m;
    }

    public void markAcked() {
        if (status == null || status == NotificationDltStatus.PENDING) {
            status = NotificationDltStatus.ACKED;
            handledAt = LocalDateTime.now();
        }
    }

    public void markResolved() {
        status = NotificationDltStatus.RESOLVED;
        handledAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = NotificationDltStatus.PENDING;
        }
    }
}
