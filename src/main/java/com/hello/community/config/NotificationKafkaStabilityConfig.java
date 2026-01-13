package com.hello.community.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;


// consumer 처리실패 시 3회재시도(1초간격)
// 3회이후에도 실패하면 ${topic}.DLT로 메시지 이동하고 재시도했던 로그 남김.
@Configuration
@ConditionalOnProperty(prefix = "app.notification.kafka", name = "enabled", havingValue = "true")
public class NotificationKafkaStabilityConfig {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaStabilityConfig.class);

    @Bean
    public DefaultErrorHandler commonErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.notification.kafka.topic:community.notification}") String topic
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(topic + ".DLT", record.partition())
        );

        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        handler.setCommitRecovered(true);

        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
            try {
                log.warn("Notification consumer retry attempt={} topic={} partition={} offset={} error={}",
                        deliveryAttempt,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        (ex != null ? ex.getClass().getSimpleName() : "null"));
            } catch (Exception ignore) {
            }
        });

        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            DefaultErrorHandler commonErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(commonErrorHandler);
        return factory;
    }

    @Bean
    public NewTopic notificationTopic(@Value("${app.notification.kafka.topic:community.notification}") String topic) {
        return new NewTopic(topic, 1, (short) 1);
    }

    @Bean
    public NewTopic notificationDltTopic(@Value("${app.notification.kafka.topic:community.notification}") String topic) {
        return new NewTopic(topic + ".DLT", 1, (short) 1);
    }
}
