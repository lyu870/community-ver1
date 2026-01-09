// PostRecommendRepository
package com.hello.community.board.recommend;

import com.hello.community.board.recommend.PostRecommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRecommendRepository extends JpaRepository<PostRecommend, Long> {

    // 이미 추천했는지 체크
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    // 추천 취소용 토글
    void deleteByPostIdAndMemberId(Long postId, Long memberId);

    // 회원탈퇴용: 특정 회원이 남긴 추천 기록 전체 조회
    List<PostRecommend> findAllByMemberId(Long memberId);

    // 회원탈퇴용: 특정 회원이 남긴 추천 기록 전체 삭제
    void deleteByMemberId(Long memberId);
}
