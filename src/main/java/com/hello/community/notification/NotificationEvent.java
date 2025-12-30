// NotificationEvent.java
package com.hello.community.notification;

import com.hello.community.notification.BoardType;
import com.hello.community.notification.NotificationType;

import java.util.List;

public record NotificationEvent(NotificationType type,
                                Long targetMemberId,
                                BoardType boardType,
                                Long postId,
                                Long focusCommentId,
                                List<Long> openReplyPath,
                                String message,
                                String eventId) {
}
