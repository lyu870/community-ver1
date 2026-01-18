// NotificationSseController.java
package com.hello.community.notification;

import com.hello.community.member.CustomUser;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationSseController {

    private final NotificationUnreadSseHub unreadSseHub;

    @GetMapping(value = "/api/notifications/unread-count/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUnreadCount(@AuthenticationPrincipal CustomUser user, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Connection", "keep-alive");

        if (user == null) {
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }

        return unreadSseHub.connect(user.getId(), 60 * 60 * 1000L);
    }
}
