// CommentService.java
package com.hello.community.comment;

import com.hello.community.board.common.BasePost;
import com.hello.community.board.common.PostFinder;
import com.hello.community.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostFinder postFinder;

    public List<Comment> getComment(Long postId) {
        BasePost post = postFinder.findPost(postId);
        return commentRepository.findAllByPostAndParentIsNullOrderByIdAsc(post);
    }

    @Transactional
    public void addComment(Long postId, Member writer, String content, Long parentId) {

        BasePost post = postFinder.findPost(postId);

        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
        }

        Comment c = Comment.create(post, writer, content, parent);
        commentRepository.save(c);
    }

    @Transactional
    public void deleteComment(Long commentId, Member loginUser) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getWriter().getId().equals(loginUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 삭제 가능합니다.");
        }

        // 대댓글이 있을 경우 → 내용만 삭제처리
        if (!comment.getChildren().isEmpty()) {
            comment.setContent("삭제된 댓글입니다.");
            return;
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public void editComment(Long commentId, Member loginUser, String newContent) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getWriter().getId().equals(loginUser.getId())) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 수정 가능합니다.");
        }

        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력하세요.");
        }

        comment.setContent(newContent);
    }
}
