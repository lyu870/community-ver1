// NoticeRepository.java
package com.hello.community.board.notice;

import com.hello.community.board.common.BasePostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends BasePostRepository<Notice> {

    @Query("""
        select n
        from Notice n
        where lower(n.title) like lower(concat('%', :keyword, '%'))
           or lower(n.content) like lower(concat('%', :keyword, '%'))
    """)
    Page<Notice> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Admin 기능: 특정 회원 게시글 목록보기
    Page<Notice> findByWriterId(Long writerId, Pageable pageable);

    // 회원탈퇴용: 특정 회원이 작성한 모든 공지 게시글 조회
    List<Notice> findAllByWriterId(Long writerId);
}
