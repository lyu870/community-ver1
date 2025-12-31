// NotificationEventConsumer.java
package com.hello.community.notification.event;

import com.hello.community.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// kafka가 on일 때에만 리스너 동작
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.notification.kafka", name = "enabled", havingValue = "true")
public class NotificationEventConsumer {

    private final NotificationEventHandler handler;

    @KafkaListener(
            topics = "${app.notification.kafka.topic:community.notification}",
            groupId = "${app.notification.kafka.group-id:community-notification}"
    )
    public void onMessage(NotificationEvent event) {
        try {
            handler.handle(event);
        } catch (Exception e) {
            return;
        }
    }
}
