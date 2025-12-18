// CommentController.java
package com.hello.community.comment;

import com.hello.community.board.common.PostFinder;
import com.hello.community.member.CustomUser;
import com.hello.community.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
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

    // 답글 lazy 로딩: fragment방식
    @GetMapping("/comment/children-fragment")
    public String loadChildrenFragment(@RequestParam Long postId,
                                       @RequestParam Long parentId,
                                       @RequestParam int depth,
                                       @RequestParam(required = false) Long loginUserId,
                                       @RequestParam(required = false) boolean isAdmin,
                                       org.springframework.ui.Model model) {

        var children = commentService.getChildren(postId, parentId);

        model.addAttribute("children", children);
        model.addAttribute("depth", depth);
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("postId", postId);
        model.addAttribute("isAdmin", isAdmin);

        return "layout/fragments/commentTree :: childrenList";
    }
}
