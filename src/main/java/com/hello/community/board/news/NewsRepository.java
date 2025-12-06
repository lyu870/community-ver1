// NewsRepository.java
package com.hello.community.board.news;

import com.hello.community.board.common.BasePostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsRepository extends BasePostRepository<News> {

    @Query("""
        select n
        from News n
        where lower(n.title) like lower(concat('%', :keyword, '%'))
           or lower(n.content) like lower(concat('%', :keyword, '%'))
    """)
    Page<News> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Admin 기능: 특정 회원 게시글 목록보기
    Page<News> findByWriterId(Long writerId, Pageable pageable);
}
