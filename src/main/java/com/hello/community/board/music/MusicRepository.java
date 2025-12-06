// MusicRepository.java
package com.hello.community.board.music;

import com.hello.community.board.common.BasePostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MusicRepository extends BasePostRepository<Music> {

    @Query("""
        select m
        from Music m
        where lower(m.title) like lower(concat('%', :keyword, '%'))
           or lower(m.content) like lower(concat('%', :keyword, '%'))
    """)
    Page<Music> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Admin 기능: 특정 회원 게시글 목록보기
    Page<Music> findByWriterId(Long writerId, Pageable pageable);

    // 회원탈퇴용: 특정 회원이 작성한 모든 음악 게시글 조회
    List<Music> findAllByWriterId(Long writerId);
}
