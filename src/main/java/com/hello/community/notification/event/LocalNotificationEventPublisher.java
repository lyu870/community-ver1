//LocalNotificationEventPublisher
package com.hello.community.notification.event;

import com.hello.community.notification.event.NotificationEvent;
import com.hello.community.notification.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

// kafka가 off일 때도 동작유지. 실패해도 아무것도막지않게함.
@Component
@RequiredArgsConstructor
@ConditionalOnMissingBean(NotificationEventPublisher.class)
public class LocalNotificationEventPublisher implements NotificationEventPublisher {

    private final NotificationEventHandler handler;

    @Override
    public void publish(NotificationEvent event) {
        try {
            handler.handle(event);
        } catch (Exception e) {
            return;
        }
    }
}
