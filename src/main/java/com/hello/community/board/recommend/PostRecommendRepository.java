// PostRecommendRepository
package com.hello.community.board.recommend;

import com.hello.community.board.recommend.PostRecommend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRecommendRepository extends JpaRepository<PostRecommend, Long> {

    // 이미 추천했는지 체크
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    // 추천 취소용 토글
    void deleteByPostIdAndMemberId(Long postId, Long memberId);
}