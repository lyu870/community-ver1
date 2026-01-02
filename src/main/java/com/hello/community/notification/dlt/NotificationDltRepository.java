// NotificationDltRepository.java
package com.hello.community.notification.dlt;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDltRepository extends JpaRepository<NotificationDltMessage, Long> {
}
