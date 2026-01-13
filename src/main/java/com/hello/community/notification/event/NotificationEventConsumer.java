package com.hello.community.notification.event;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// kafka가 on일 때에만 리스너 동작
// handler에서 예외나오면 DefaultErrorHandler가 재시도해서 DLT로 이동
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.notification.kafka", name = "enabled", havingValue = "true")
public class NotificationEventConsumer {

    private final NotificationEventHandler handler;

    @KafkaListener(
            topics = "${app.notification.kafka.topic:community.notification}",
            groupId = "${app.notification.kafka.group-id:community-notification}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<Object, NotificationEvent> record) {
        NotificationEvent event = (record != null ? record.value() : null);

        if (event == null) {
            throw new IllegalStateException("NotificationEvent is null");
        }

        validateEvent(event);

        handler.handle(event);
    }

    private void validateEvent(NotificationEvent event) {
        if (event.getType() == null) {
            throw new IllegalStateException("NotificationEvent.type is null");
        }
        if (event.getTargetMemberId() == null) {
            throw new IllegalStateException("NotificationEvent.targetMemberId is null");
        }
        if (event.getBoardType() == null) {
            throw new IllegalStateException("NotificationEvent.boardType is null");
        }

        Long postId = event.getPostId();
        if (postId == null || postId <= 0) {
            throw new IllegalStateException("NotificationEvent.postId is invalid");
        }

        String eventId = event.getEventId();
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalStateException("NotificationEvent.eventId is blank");
        }
    }
}
