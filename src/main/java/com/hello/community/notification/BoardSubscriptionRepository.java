// BoardSubscriptionRepository.java
package com.hello.community.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardSubscriptionRepository extends JpaRepository<BoardSubscription, Long> {

    List<BoardSubscription> findByMemberId(Long memberId);

    Optional<BoardSubscription> findByMemberIdAndBoardType(Long memberId, BoardType boardType);

    List<BoardSubscription> findByBoardTypeAndEnabledTrue(BoardType boardType);
}
