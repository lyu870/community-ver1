// NotificationSseController.java
package com.hello.community.notification;

import com.hello.community.member.CustomUser;
import com.hello.community.notification.dto.UnreadCountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationSseController {

    private final NotificationService notificationService;
    private final NotificationUnreadSseHub unreadSseHub;

    @GetMapping(value = "/api/notifications/unread-count/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUnreadCount(@AuthenticationPrincipal CustomUser user) {
        SseEmitter emitter = unreadSseHub.connect((user != null) ? user.getId() : null, 60 * 60 * 1000L);

        if (user == null) {
            emitter.complete();
            return emitter;
        }

        UnreadCountResponseDto dto = notificationService.getUnreadCount(user.getId());
        long count = (dto != null) ? dto.getUnreadCount() : 0L;

        unreadSseHub.sendUnreadCount(user.getId(), count);

        return emitter;
    }
}
