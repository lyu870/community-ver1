// NotificationEvent.java
package com.hello.community.notification.event;

import com.hello.community.notification.BoardType;
import com.hello.community.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private Long targetMemberId;
    private NotificationType type;

    private BoardType boardType;
    private Long postId;

    private Long focusCommentId;
    private List<Long> openReplyPath;

    private String message;
    private String eventId;
}
