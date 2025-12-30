// SyncNotificationEventPublisher.java
package com.hello.community.notification;

import com.hello.community.notification.NotificationEventPublisher;
import com.hello.community.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SyncNotificationEventPublisher implements NotificationEventPublisher {

    private final NotificationService notificationService;

    @Override
    public void publish(NotificationEvent event) {
        if (event == null) {
            return;
        }

        try {
            if (event.type() == NotificationType.POST_COMMENT) {
                notificationService.createPostCommentNotification(
                        event.targetMemberId(),
                        event.boardType(),
                        event.postId(),
                        event.focusCommentId(),
                        event.message(),
                        event.eventId()
                );
                return;
            }

            if (event.type() == NotificationType.COMMENT_REPLY) {
                notificationService.createCommentReplyNotification(
                        event.targetMemberId(),
                        event.boardType(),
                        event.postId(),
                        event.openReplyPath(),
                        event.focusCommentId(),
                        event.message(),
                        event.eventId()
                );
            }
        } catch (Exception e) {
            return;
        }
    }
}
