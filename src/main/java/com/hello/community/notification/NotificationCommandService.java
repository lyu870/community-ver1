// NotificationCommandService.java
package com.hello.community.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification save(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        notificationRepository.flush();
        return saved;
    }
}
