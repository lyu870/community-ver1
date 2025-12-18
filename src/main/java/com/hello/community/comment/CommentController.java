// CommentController.java
package com.hello.community.comment;

import com.hello.community.board.common.PostFinder;
import com.hello.community.member.CustomUser;
import com.hello.community.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostFinder postFinder;

    @PostMapping("/comment")
    public String writeComment(@RequestParam Long postId,
                               @RequestParam String content,
                               @RequestParam(required = false) Long parentId,
                               @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        Member writer = user.getMember();
        Long savedCommentId = commentService.addComment(postId, writer, content, parentId);

        String detailUrl = postFinder.buildDetailUrl(postId);

        // 답글/대대댓글 작성 후: root -> ... -> parentId 경로를 열고, 새 댓글로 스크롤
        if (parentId != null) {
            List<Long> path = commentService.buildAncestorPathInclusive(parentId);
            String pathParam = URLEncoder.encode(
                    String.join(",", path.stream().map(String::valueOf).toList()),
                    StandardCharsets.UTF_8
            );

            return "redirect:" + detailUrl
                    + "?openReplyPath=" + pathParam
                    + "&focusCommentId=" + savedCommentId;
        }

        // 루트 댓글 작성 후: 작성한 댓글로 스크롤
        return "redirect:" + detailUrl + "?focusCommentId=" + savedCommentId;
    }

    @PostMapping("/comment/delete")
    public String deleteComment(@RequestParam Long id,
                                @RequestParam Long postId,
                                @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        Member writer = user.getMember();
        commentService.deleteComment(id, writer);

        return "redirect:" + postFinder.buildDetailUrl(postId);
    }

    @PostMapping("/comment/edit")
    public String editComment(@RequestParam Long id,
                              @RequestParam Long postId,
                              @RequestParam String content,
                              @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        Member writer = user.getMember();
        commentService.editComment(id, writer, content);

        String detailUrl = postFinder.buildDetailUrl(postId);

        // 댓글/답글 수정 후: 수정한 댓글로 자동 스크롤
        Long parentId = commentService.findParentId(id);
        if (parentId != null) {
            List<Long> path = commentService.buildAncestorPathInclusive(parentId);
            String pathParam = URLEncoder.encode(
                    String.join(",", path.stream().map(String::valueOf).toList()),
                    StandardCharsets.UTF_8
            );

            return "redirect:" + detailUrl
                    + "?openReplyPath=" + pathParam
                    + "&focusCommentId=" + id;
        }

        return "redirect:" + detailUrl + "?focusCommentId=" + id;
    }

    // 답글 lazy 로딩: JSON API방식 (페이징)
    @GetMapping({"/comment/children", "/children"})
    @ResponseBody
    public ResponseEntity<ChildrenPageResponseDto> loadChildrenJson(@RequestParam Long postId,
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

        return ResponseEntity.ok(body);
    }

    // 답글 lazy 로딩: fragment방식
    @GetMapping("/comment/children-fragment")
    public String loadChildrenFragment(@RequestParam Long postId,
                                       @RequestParam Long parentId,
                                       @RequestParam int depth,
                                       @RequestParam(required = false) Long loginUserId,
                                       @RequestParam(required = false) boolean isAdmin,
                                       Model model) {

        var children = commentService.getChildren(postId, parentId);

        model.addAttribute("children", children);
        model.addAttribute("depth", depth);
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("postId", postId);
        model.addAttribute("isAdmin", isAdmin);

        return "layout/fragments/commentTree :: childrenList";
    }

    static class ChildCommentDto {

        private Long id;
        private Long writerId;
        private String writerDisplayName;
        private String content;
        private String createdAt;
        private String updatedAt;
        private long replyCount;

        public ChildCommentDto(Long id,
                               Long writerId,
                               String writerDisplayName,
                               String content,
                               String createdAt,
                               String updatedAt,
                               long replyCount) {
            this.id = id;
            this.writerId = writerId;
            this.writerDisplayName = writerDisplayName;
            this.content = content;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.replyCount = replyCount;
        }

        public static ChildCommentDto from(Comment c) {
            Long writerId = (c.getWriter() != null) ? c.getWriter().getId() : null;
            String writerDisplayName = (c.getWriter() != null) ? c.getWriter().getDisplayName() : "";
            String createdAt = (c.getCreatedAt() != null) ? c.getCreatedAt().toString() : null;
            String updatedAt = (c.getUpdatedAt() != null) ? c.getUpdatedAt().toString() : null;

            return new ChildCommentDto(
                    c.getId(),
                    writerId,
                    writerDisplayName,
                    c.getContent(),
                    createdAt,
                    updatedAt,
                    c.getReplyCount()
            );
        }

        public Long getId() {
            return id;
        }

        public Long getWriterId() {
            return writerId;
        }

        public String getWriterDisplayName() {
            return writerDisplayName;
        }

        public String getContent() {
            return content;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public long getReplyCount() {
            return replyCount;
        }
    }

    static class ChildrenPageResponseDto {

        private List<ChildCommentDto> items;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;

        public ChildrenPageResponseDto(List<ChildCommentDto> items,
                                       int page,
                                       int size,
                                       long totalElements,
                                       int totalPages,
                                       boolean hasNext) {
            this.items = items;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
        }

        public List<ChildCommentDto> getItems() {
            return items;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public boolean isHasNext() {
            return hasNext;
        }
    }
}
