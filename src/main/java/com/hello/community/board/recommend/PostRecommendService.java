// PostRecommendService.java
package com.hello.community.board.recommend;

import com.hello.community.board.recommend.PostRecommend;
import com.hello.community.board.recommend.PostRecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostRecommendService {

    private final PostRecommendRepository postRecommendRepository;

    public boolean isRecommended(Long postId, Long memberId) {
        return postRecommendRepository.existsByPostIdAndMemberId(postId, memberId);
    }

    /*
     * 토글:
     *  - 기존에 추천 안했으면 → 추천 기록 생성 후 true 반환
     *  - 이미 추천했으면 → 기록 삭제 후 false 반환
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
            return true;  // 추천한 상태
        }
    }
}
