// NotificationRepository.java
package com.hello.community.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByMemberId(Long memberId, Pageable pageable);

    long countByMemberIdAndReadAtIsNull(Long memberId);

    Optional<Notification> findByIdAndMemberId(Long id, Long memberId);

    List<Notification> findByMemberIdAndIdIn(Long memberId, Collection<Long> ids);

    boolean existsByEventId(String eventId);
}
