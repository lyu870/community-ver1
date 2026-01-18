package com.hello.community.notification;

import com.hello.community.member.CustomUser;
import com.hello.community.notification.dto.UnreadCountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class NotificationSseController {

    private final NotificationService notificationService;
    private final NotificationUnreadSseHub unreadSseHub;

    @GetMapping(value = "/api/notifications/unread-count/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUnreadCount(@AuthenticationPrincipal CustomUser user) {
        if (user == null) {
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }

        SseEmitter emitter = unreadSseHub.connect(user.getId(), 60 * 60 * 1000L);

        UnreadCountResponseDto dto = notificationService.getUnreadCount(user.getId());
        long count = (dto != null) ? dto.getUnreadCount() : 0L;

        try {
            emitter.send(SseEmitter.event()
                    .name("unread-count")
                    .data(new UnreadCountResponseDto(count)));
        } catch (IOException e) {
        }

        return emitter;
    }
}
