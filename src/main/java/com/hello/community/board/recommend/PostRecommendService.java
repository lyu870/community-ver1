// PostRecommendService.java
package com.hello.community.board.recommend;

import com.hello.community.board.common.BasePost;
import com.hello.community.board.music.MusicRepository;
import com.hello.community.board.news.NewsRepository;
import com.hello.community.board.notice.NoticeRepository;
import com.hello.community.member.Member;
import com.hello.community.member.MemberRepository;
import com.hello.community.notification.NotificationService;
import com.hello.community.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
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
    private final NotificationService notificationService;

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
            createPostRecommendNotificationSafely(postId, memberId, recommendId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                createPostRecommendNotificationSafely(postId, memberId, recommendId);
            }
        });
    }

    private BasePost findPostById(Long postId) {
        if (postId == null) {
            return null;
        }

        BasePost post = musicRepository.findById(postId).orElse(null);
        if (post != null) {
            return post;
        }

        post = newsRepository.findById(postId).orElse(null);
        if (post != null) {
            return post;
        }

        return noticeRepository.findById(postId).orElse(null);
    }

    private void createPostRecommendNotificationSafely(Long postId, Long memberId, Long recommendId) {
        try {
            BasePost post = findPostById(postId);
            if (post == null || post.getWriter() == null || post.getWriter().getId() == null) {
                return;
            }

            Long targetMemberId = post.getWriter().getId();
            if (Objects.equals(targetMemberId, memberId)) {
                return;
            }

            Member actor = memberRepository.findById(memberId).orElse(null);
            String actorName = (actor != null && actor.getDisplayName() != null && !actor.getDisplayName().trim().isEmpty())
                    ? actor.getDisplayName()
                    : "누군가";

            String boardPath = "";
            try {
                String simple = Hibernate.getClass(post).getSimpleName();
                if (simple != null) {
                    boardPath = simple.toLowerCase();
                }
            } catch (Exception e) {
                boardPath = "";
            }

            if (boardPath.trim().isEmpty()) {
                return;
            }

            String linkUrl = "/" + boardPath + "/detail/" + postId;

            String eventId = (recommendId != null)
                    ? "POST_RECOMMEND:" + recommendId
                    : "POST_RECOMMEND:" + postId + ":" + memberId;

            String message = actorName + "님이 내 게시글을 추천했습니다.";

            notificationService.createNotification(
                    targetMemberId,
                    NotificationType.POST_RECOMMEND,
                    message,
                    linkUrl,
                    eventId
            );
        } catch (Exception e) {
            return;
        }
    }
}
