package com.hello.community.notification.dlt;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// DB적재하는 기능.
// 이 파일에서 예외를 안던지고 로그만 남겨야됨 (안그러면.. DLT무한루프 생김..)
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.notification.kafka", name = "enabled", havingValue = "true")
public class NotificationDltConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationDltConsumer.class);

    private final NotificationDltService notificationDltService;

    @KafkaListener(
            topics = "${app.notification.kafka.topic:community.notification}.DLT",
            groupId = "${app.notification.kafka.group-id:community-notification}-dlt",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDltMessage(ConsumerRecord<Object, Object> record) {
        try {
            notificationDltService.saveFromRecord(record);
        } catch (Exception e) {
            try {
                log.error("Notification DLT save failed topic={} partition={} offset={} error={}",
                        (record != null ? record.topic() : "null"),
                        (record != null ? record.partition() : -1),
                        (record != null ? record.offset() : -1),
                        e.getClass().getSimpleName(), e);
            } catch (Exception ignore) {
            }
        }
    }
}
