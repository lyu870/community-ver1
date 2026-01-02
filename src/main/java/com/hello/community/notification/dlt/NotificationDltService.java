// NotificationDltService.java
package com.hello.community.notification.dlt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hello.community.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

// DLT 저장하는 서비스.
@Service
@RequiredArgsConstructor
public class NotificationDltService {

    private final NotificationDltRepository notificationDltRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveFromRecord(ConsumerRecord<Object, NotificationEvent> record) {
        if (record == null) {
            return;
        }

        NotificationEvent event = record.value();

        String payloadJson = null;
        try {
            payloadJson = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            payloadJson = (event != null ? String.valueOf(event) : null);
        }

        String originalTopic = headerString(record.headers(), KafkaHeaders.DLT_ORIGINAL_TOPIC);
        Integer originalPartition = headerInt(record.headers(), KafkaHeaders.DLT_ORIGINAL_PARTITION);
        Long originalOffset = headerLong(record.headers(), KafkaHeaders.DLT_ORIGINAL_OFFSET);

        String exceptionFqcn = headerString(record.headers(), KafkaHeaders.DLT_EXCEPTION_FQCN);
        String exceptionMessage = headerString(record.headers(), KafkaHeaders.DLT_EXCEPTION_MESSAGE);
        String exceptionStacktrace = headerString(record.headers(), KafkaHeaders.DLT_EXCEPTION_STACKTRACE);

        String recordKey = (record.key() != null ? String.valueOf(record.key()) : null);

        String eventId = null;
        String type = null;
        Long targetMemberId = null;

        if (event != null) {
            eventId = event.getEventId();
            type = (event.getType() != null ? event.getType().name() : null);
            targetMemberId = event.getTargetMemberId();
        }

        NotificationDltMessage saved = NotificationDltMessage.of(
                record.topic(),
                record.partition(),
                record.offset(),
                originalTopic,
                originalPartition,
                originalOffset,
                recordKey,
                eventId,
                type,
                targetMemberId,
                payloadJson,
                exceptionFqcn,
                exceptionMessage,
                exceptionStacktrace
        );

        notificationDltRepository.save(saved);
    }

    private String headerString(Headers headers, String key) {
        if (headers == null || key == null) {
            return null;
        }

        Header h = headers.lastHeader(key);
        if (h == null || h.value() == null) {
            return null;
        }

        try {
            return new String(h.value(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer headerInt(Headers headers, String key) {
        String raw = headerString(headers, key);
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long headerLong(Headers headers, String key) {
        String raw = headerString(headers, key);
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
