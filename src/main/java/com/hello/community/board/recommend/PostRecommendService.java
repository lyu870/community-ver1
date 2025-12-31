// PostRecommendService.java
package com.hello.community.board.recommend;

import com.hello.community.board.music.Music;
import com.hello.community.board.music.MusicRepository;
import com.hello.community.board.news.News;
import com.hello.community.board.news.NewsRepository;
import com.hello.community.board.notice.Notice;
import com.hello.community.board.notice.NoticeRepository;
import com.hello.community.member.Member;
import com.hello.community.member.MemberRepository;
import com.hello.community.notification.BoardType;
import com.hello.community.notification.NotificationType;
import com.hello.community.notification.event.NotificationEvent;
import com.hello.community.notification.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostRecommendService {

    private final PostRecommendRepository postRecommendRepository;

    private final MusicRepository musicRepository;
    private final NewsRepository newsRepository;
    private final NoticeRepository noticeRepository;

    private final MemberRepository memberRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    public boolean isRecommended(Long postId, Long memberId) {
        return postRecommendRepository.existsByPostIdAndMemberId(postId, memberId);
    }

    /*
     * 토글:
     * 기존에 추천 안했으면 → 추천 기록 생성 후 true 반환
     * 이미 추천했으면 → 기록 삭제 후 false 반환
     */
    @Transactional
    public boolean toggleRecommend(Long postId, Long memberId) {
        boolean exists = isRecommended(postId, memberId);
        if (exists) {
            postRecommendRepository.deleteByPostIdAndMemberId(postId, memberId);
            return false; // 추천안한 상태
        } else {
            PostRecommend rec = new PostRecommend();
            rec.setPostId(postId);
            rec.setMemberId(memberId);
            postRecommendRepository.save(rec);

            Long recommendId = rec.getId();
            publishPostRecommendNotificationAfterCommit(postId, memberId, recommendId);

            return true;  // 추천한 상태
        }
    }

    // 회원탈퇴 시: 해당 회원이 남긴 추천 기록 전체 삭제
    @Transactional
    public void deleteRecommendsForWithdraw(Long memberId) {
        postRecommendRepository.deleteByMemberId(memberId);
    }

    // 필요하면 탈퇴 전에 추천 기록 목록이 필요한 경우를 대비
    public List<PostRecommend> getRecommendsByMemberId(Long memberId) {
        return postRecommendRepository.findAllByMemberId(memberId);
    }

    private void publishPostRecommendNotificationAfterCommit(Long postId, Long memberId, Long recommendId) {
        if (postId == null || memberId == null) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publishPostRecommendEventSafely(postId, memberId, recommendId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publishPostRecommendEventSafely(postId, memberId, recommendId);
            }
        });
    }

    private void publishPostRecommendEventSafely(Long postId, Long memberId, Long recommendId) {
        try {
            PostAndBoard found = findPostAndBoardType(postId);
            if (found == null || found.targetMemberId == null || found.boardType == null) {
                return;
            }

            if (Objects.equals(found.targetMemberId, memberId)) {
                return;
            }

            Member actor = memberRepository.findById(memberId).orElse(null);
            String actorName = (actor != null && actor.getDisplayName() != null && !actor.getDisplayName().trim().isEmpty())
                    ? actor.getDisplayName()
                    : "누군가";

            String eventId = (recommendId != null)
                    ? "POST_RECOMMEND:" + recommendId
                    : "POST_RECOMMEND:" + postId + ":" + memberId;

            String message = actorName + "님이 내 게시글을 추천했습니다.";

            NotificationEvent event = new NotificationEvent(
                    found.targetMemberId,
                    NotificationType.POST_RECOMMEND,
                    found.boardType,
                    postId,
                    null,
                    null,
                    message,
                    eventId
            );

            notificationEventPublisher.publish(event);
        } catch (Exception e) {
            return;
        }
    }

    private PostAndBoard findPostAndBoardType(Long postId) {
        Music m = musicRepository.findById(postId).orElse(null);
        if (m != null && m.getWriter() != null && m.getWriter().getId() != null) {
            return new PostAndBoard(m.getWriter().getId(), BoardType.MUSIC);
        }

        News n = newsRepository.findById(postId).orElse(null);
        if (n != null && n.getWriter() != null && n.getWriter().getId() != null) {
            return new PostAndBoard(n.getWriter().getId(), BoardType.NEWS);
        }

        Notice no = noticeRepository.findById(postId).orElse(null);
        if (no != null && no.getWriter() != null && no.getWriter().getId() != null) {
            return new PostAndBoard(no.getWriter().getId(), BoardType.NOTICE);
        }

        return null;
    }

    private static class PostAndBoard {
        private Long targetMemberId;
        private BoardType boardType;

        private PostAndBoard(Long targetMemberId, BoardType boardType) {
            this.targetMemberId = targetMemberId;
            this.boardType = boardType;
        }
    }
}
