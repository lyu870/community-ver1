// CommentApiController.java
package com.hello.community.comment;

import com.hello.community.board.common.ApiResponseDto;
import com.hello.community.member.CustomUser;
import com.hello.community.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;

    // 답글 lazy 로딩: JSON API방식 (페이징)
    @GetMapping("/api/comment/children")
    public ResponseEntity<ApiResponseDto<ChildrenPageResponseDto>> loadChildrenJson(@RequestParam Long postId,
                                                                                    @RequestParam Long parentId,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "10") int size) {

        if (size < 1) {
            size = 10;
        }

        if (size > 50) {
            size = 50;
        }

        if (page < 0) {
            page = 0;
        }

        Page<Comment> childrenPage = commentService.getChildrenPage(postId, parentId, page, size);

        List<ChildCommentDto> items = childrenPage.getContent().stream()
                .map(ChildCommentDto::from)
                .toList();

        ChildrenPageResponseDto body = new ChildrenPageResponseDto(
                items,
                childrenPage.getNumber(),
                childrenPage.getSize(),
                childrenPage.getTotalElements(),
                childrenPage.getTotalPages(),
                childrenPage.hasNext()
        );

        return ResponseEntity.ok(ApiResponseDto.ok(body));
    }

    // 댓글/답글 등록: JSON API방식
    @PostMapping("/api/comment")
    public ResponseEntity<ApiResponseDto<ChildCommentDto>> writeCommentJson(@RequestParam Long postId,
                                                                            @RequestParam String content,
                                                                            @RequestParam(required = false) Long parentId,
                                                                            @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
        }

        Member writer = user.getMember();

        Comment saved = commentService.addCommentAndReturn(postId, writer, content, parentId);
        ChildCommentDto body = ChildCommentDto.from(saved);

        return ResponseEntity.ok(ApiResponseDto.ok(body));
    }

    // 댓글/답글 수정: JSON API방식
    @PutMapping("/api/comment/{id}")
    public ResponseEntity<ApiResponseDto<ChildCommentDto>> editCommentJson(@PathVariable Long id,
                                                                           @RequestParam String content,
                                                                           @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
        }

        Member writer = user.getMember();

        Comment updated = commentService.editCommentAndReturn(id, writer, content);
        ChildCommentDto body = ChildCommentDto.from(updated);

        return ResponseEntity.ok(ApiResponseDto.ok(body));
    }

    // 댓글/답글 삭제: JSON API방식
    @DeleteMapping("/api/comment/{id}")
    public ResponseEntity<ApiResponseDto<Long>> deleteCommentJson(@PathVariable Long id,
                                                                  @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
        }

        Member writer = user.getMember();
        boolean isAdmin = user.isAdmin();

        commentService.deleteComment(id, writer, isAdmin);

        return ResponseEntity.ok(ApiResponseDto.ok(id));
    }
}
