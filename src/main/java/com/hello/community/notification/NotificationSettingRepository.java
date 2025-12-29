// NotificationSettingRepository.java
package com.hello.community.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    Optional<NotificationSetting> findByMemberId(Long memberId);
}
