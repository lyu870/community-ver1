// NotificationEventPublisher.java
package com.hello.community.notification;
import com.hello.community.notification.NotificationEvent;//

public interface NotificationEventPublisher {
    void publish(NotificationEvent event);
}
