// CommentRepository.java
package com.hello.community.comment;

import com.hello.community.board.common.BasePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글 상세에서 최상위 댓글만 가져오기 (대댓글 제외)
    List<Comment> findAllByPostAndParentIsNullOrderByIdAsc(BasePost post);

    // 게시글별 댓글 개수
    long countByPost(BasePost post);

    // Admin 기능: 특정 회원이 작성한 모든 댓글 조회
    Page<Comment> findByWriterId(Long writerId, Pageable pageable);
}
