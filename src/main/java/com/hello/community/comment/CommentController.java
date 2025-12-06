// CommentController.java
package com.hello.community.comment;

import com.hello.community.board.common.PostFinder;
import com.hello.community.member.CustomUser;
import com.hello.community.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

        return "redirect:" + postFinder.buildDetailUrl(postId);
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
}
