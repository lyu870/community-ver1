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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

// DLT 저장하는 서비스.
@Service
@RequiredArgsConstructor
public class NotificationDltService {

    private final NotificationDltRepository notificationDltRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveFromRecord(ConsumerRecord<Object, Object> record) {
        if (record == null) {
            return;
        }

        Object valueObj = record.value();

        NotificationEvent event = null;
        if (valueObj instanceof NotificationEvent) {
            event = (NotificationEvent) valueObj;
        } else if (valueObj != null) {
            try {
                event = objectMapper.convertValue(valueObj, NotificationEvent.class);
            } catch (Exception ignore) {
            }
        }

        String payloadJson = null;
        try {
            payloadJson = objectMapper.writeValueAsString(valueObj);
        } catch (Exception e) {
            payloadJson = (valueObj != null ? String.valueOf(valueObj) : null);
        }

        String originalTopic = headerString(record.headers(), KafkaHeaders.DLT_ORIGINAL_TOPIC);
        Integer originalPartition = headerInt(record.headers(), KafkaHeaders.DLT_ORIGINAL_PARTITION);
        Long originalOffset = headerLong(record.headers(), KafkaHeaders.DLT_ORIGINAL_OFFSET);

        if (originalTopic == null || originalTopic.isBlank()) {
            String topic = record.topic();
            if (topic != null && topic.endsWith(".DLT")) {
                originalTopic = topic.substring(0, topic.length() - 4);
            } else {
                originalTopic = topic;
            }
        }
        if (originalPartition == null) {
            originalPartition = record.partition();
        }
        if (originalOffset == null) {
            originalOffset = record.offset();
        }

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

    private byte[] headerBytes(Headers headers, String key) {
        if (headers == null || key == null) {
            return null;
        }

        Header h = headers.lastHeader(key);
        if (h == null) {
            return null;
        }

        return h.value();
    }

    private String headerString(Headers headers, String key) {
        byte[] raw = headerBytes(headers, key);
        if (raw == null || raw.length == 0) {
            return null;
        }

        try {
            return new String(raw, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer headerInt(Headers headers, String key) {
        byte[] raw = headerBytes(headers, key);
        if (raw == null || raw.length == 0) {
            return null;
        }

        try {
            String s = new String(raw, StandardCharsets.UTF_8).trim();
            if (!s.isBlank() && s.chars().allMatch(Character::isDigit)) {
                return Integer.parseInt(s);
            }
        } catch (Exception ignore) {
        }

        try {
            if (raw.length == 4) {
                return ByteBuffer.wrap(raw).getInt();
            }
        } catch (Exception ignore) {
        }

        return null;
    }

    private Long headerLong(Headers headers, String key) {
        byte[] raw = headerBytes(headers, key);
        if (raw == null || raw.length == 0) {
            return null;
        }

        try {
            String s = new String(raw, StandardCharsets.UTF_8).trim();
            if (!s.isBlank() && s.chars().allMatch(Character::isDigit)) {
                return Long.parseLong(s);
            }
        } catch (Exception ignore) {
        }

        try {
            if (raw.length == 8) {
                return ByteBuffer.wrap(raw).getLong();
            }
        } catch (Exception ignore) {
        }

        return null;
    }
}
