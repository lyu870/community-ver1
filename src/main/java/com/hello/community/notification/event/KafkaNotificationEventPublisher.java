// KafkaNotificationEventPublisher.java
package com.hello.community.notification.event;

import com.hello.community.notification.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// app.notification.kafka.enabled=true 일 때만 Bean 생성
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.notification.kafka", name = "enabled", havingValue = "true")
public class KafkaNotificationEventPublisher implements NotificationEventPublisher {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${app.notification.kafka.topic:community.notification}")
    private String topic;

    @Override
    public void publish(NotificationEvent event) {
        if (event == null) {
            return;
        }

        String key = (event.getEventId() != null && !event.getEventId().isBlank())
                ? event.getEventId()
                : String.valueOf(event.getTargetMemberId());

        try {
            kafkaTemplate.send(topic, key, event);
        } catch (Exception e) {
            return;
        }
    }
}
