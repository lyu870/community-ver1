// NotificationEventHandler.java
package com.hello.community.notification.event;

import com.hello.community.notification.NotificationService;
import com.hello.community.notification.NotificationType;
import com.hello.community.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 알림저장은 이 파일에서만 하게만들기.
// REQUIRES_NEW 로 분리해서(consumer든 local이든) 안정적으로 저장
@Service
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(NotificationEvent event) {
        if (event == null) {
            return;
        }

        if (event.getTargetMemberId() == null || event.getType() == null) {
            return;
        }

        if (event.getMessage() == null || event.getMessage().trim().isEmpty()) {
            return;
        }

        if (event.getType() == NotificationType.POST_COMMENT) {
            if (event.getBoardType() == null || event.getPostId() == null || event.getFocusCommentId() == null) {
                return;
            }

            notificationService.createPostCommentNotification(
                    event.getTargetMemberId(),
                    event.getBoardType(),
                    event.getPostId(),
                    event.getFocusCommentId(),
                    event.getMessage(),
                    event.getEventId()
            );

            return;
        }

        if (event.getType() == NotificationType.COMMENT_REPLY) {
            if (event.getBoardType() == null || event.getPostId() == null || event.getFocusCommentId() == null) {
                return;
            }

            notificationService.createCommentReplyNotification(
                    event.getTargetMemberId(),
                    event.getBoardType(),
                    event.getPostId(),
                    event.getOpenReplyPath(),
                    event.getFocusCommentId(),
                    event.getMessage(),
                    event.getEventId()
            );

            return;
        }
    }
}
