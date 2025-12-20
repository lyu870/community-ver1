// CommentApiController.java
package com.hello.community.comment;

import com.hello.community.board.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
}
