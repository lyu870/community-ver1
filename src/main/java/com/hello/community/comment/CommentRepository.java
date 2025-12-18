// CommentRepository.java
package com.hello.community.comment;

import com.hello.community.board.common.BasePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글 상세에서 최상위 댓글만 가져오기 (대댓글 제외)
    @Query("""
            select c
            from Comment c
            join fetch c.writer w
            where c.post = :post
              and c.parent is null
            order by c.id asc
            """)
    List<Comment> findRootByPostWithWriter(@Param("post") BasePost post);

    // lazy 로딩: 특정 부모 댓글의 직계 답글만 가져오기
    @Query("""
            select c
            from Comment c
            join fetch c.writer w
            where c.post = :post
              and c.parent.id = :parentId
            order by c.id asc
            """)
    List<Comment> findChildrenByPostAndParentIdWithWriter(@Param("post") BasePost post,
                                                          @Param("parentId") Long parentId);

    // lazy 로딩: 특정 부모 댓글의 직계 답글 페이징 조회
    // EntityGraph로 writer만 가져오도록 처리
    @EntityGraph(attributePaths = {"writer"})
    @Query("""
            select c
            from Comment c
            where c.post = :post
              and c.parent.id = :parentId
            order by c.id asc
            """)
    Page<Comment> findChildrenPageByPostAndParentId(@Param("post") BasePost post,
                                                    @Param("parentId") Long parentId,
                                                    Pageable pageable);

    // 게시글별 댓글 개수
    long countByPost(BasePost post);

    // Admin 기능: 특정 회원이 작성한 모든 댓글 조회 (페이지네이션)
    Page<Comment> findByWriterId(Long writerId, Pageable pageable);

    // 회원탈퇴용: 특정 회원이 작성한 모든 댓글 조회 (전체)
    List<Comment> findByWriterId(Long writerId);

    // 답글 개수 계산용: post 내 모든 댓글의 (id, parentId)만 조회
    @Query("""
            select c.id as id,
                   c.parent.id as parentId
            from Comment c
            where c.post = :post
            """)
    List<CommentIdParentView> findIdAndParentIdByPost(@Param("post") BasePost post);

    interface CommentIdParentView {
        Long getId();
        Long getParentId();
    }
}
