// CommentController.java
package com.hello.community.comment;

import com.hello.community.board.common.PostFinder;
import com.hello.community.member.CustomUser;
import com.hello.community.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        commentService.addComment(postId, writer, content, parentId);

        String detailUrl = postFinder.buildDetailUrl(postId);

        // 답글 작성 후에는 해당 부모댓글의 답글이 펼쳐진 상태로 리로드
        if (parentId != null) {
            return "redirect:" + detailUrl + "?openReplyParentId=" + parentId;
        }

        return "redirect:" + detailUrl;
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

        return "redirect:" + postFinder.buildDetailUrl(postId);
    }

    // 답글 lazy 로딩: JSON API 방식
    @GetMapping({"/comment/children", "/children"})
    @ResponseBody
    public ResponseEntity<List<ChildCommentDto>> loadChildrenJson(@RequestParam Long postId,
                                                                  @RequestParam Long parentId) {

        var children = commentService.getChildren(postId, parentId);

        List<ChildCommentDto> body = children.stream()
                .map(ChildCommentDto::from)
                .toList();

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

    // 답글 JSON 반환용 DTO
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
}
