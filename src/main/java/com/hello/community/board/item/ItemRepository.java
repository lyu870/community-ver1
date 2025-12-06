// ItemRepository.java
package com.hello.community.board.item;

import com.hello.community.board.common.BasePostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends BasePostRepository<Item> {

    @Query("""
           select i
           from Item i
           where lower(i.title) like lower(concat('%', :keyword, '%'))
              or lower(i.content) like lower(concat('%', :keyword, '%'))
           """)
    Page<Item> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Admin 기능: 특정 회원 게시글 목록보기
    Page<Item> findByWriterId(Long writerId, Pageable pageable);

    // 회원탈퇴용: 특정 회원이 작성한 모든 아이템 게시글 조회
    List<Item> findAllByWriterId(Long writerId);
}
