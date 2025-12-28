// AdminController.java
package com.hello.community.admin;

import com.hello.community.board.common.BasePost;
import com.hello.community.board.music.Music;
import com.hello.community.board.music.MusicRepository;
import com.hello.community.board.news.News;
import com.hello.community.board.news.NewsRepository;
import com.hello.community.board.notice.Notice;
import com.hello.community.board.notice.NoticeRepository;
import com.hello.community.comment.Comment;
import com.hello.community.comment.CommentRepository;
import com.hello.community.member.Member;
import com.hello.community.member.MemberRepository;
import com.hello.community.board.common.PageUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MemberRepository memberRepository;
    private final NewsRepository newsRepository;
    private final MusicRepository musicRepository;
    private final NoticeRepository noticeRepository;
    private final CommentRepository commentRepository;

    // 1. 관리자 - 회원 목록 페이지
    @GetMapping("/members")
    public String members(@RequestParam(defaultValue = "1") int page,
                          Model model) {

        int pageSize = 10;
        Page<Member> memberPage =
                memberRepository.findAll(
                        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
                );

        List<Member> items = memberPage.getContent();

        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(memberPage, 5));

        return "admin/members";
    }

    // 2. 관리자 - 특정 회원의 게시글 전체
    @GetMapping("/member/{id}/posts")
    public String memberPosts(@PathVariable Long id,
                              @RequestParam(defaultValue = "1") int newsPage,
                              @RequestParam(defaultValue = "1") int musicPage,
                              @RequestParam(defaultValue = "1") int noticePage,
                              Model model) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다. id=" + id));

        int pageSize = 10;

        if (newsPage < 1) newsPage = 1;
        if (musicPage < 1) musicPage = 1;
        if (noticePage < 1) noticePage = 1;

        Page<News> newsPageResult = newsRepository.findByWriterId(
                id,
                PageRequest.of(newsPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Page<Music> musicPageResult = musicRepository.findByWriterId(
                id,
                PageRequest.of(musicPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Page<Notice> noticePageResult = noticeRepository.findByWriterId(
                id,
                PageRequest.of(noticePage - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("news", newsPageResult.getContent());
        model.addAttribute("music", musicPageResult.getContent());
        model.addAttribute("notice", noticePageResult.getContent());

        model.addAttribute("memberId", id);
        model.addAttribute("memberDisplayName", member.getDisplayName());

        model.addAllAttributes(PageUtil.buildPageModel(newsPageResult, 5, "news"));
        model.addAllAttributes(PageUtil.buildPageModel(musicPageResult, 5, "music"));
        model.addAllAttributes(PageUtil.buildPageModel(noticePageResult, 5, "notice"));

        boolean hasNotice = noticePageResult.getTotalElements() > 0;
        model.addAttribute("hasNotice", hasNotice);

        return "admin/member-posts";
    }

    // 3. 관리자 - 특정 회원의 댓글 전체
    @GetMapping("/member/{id}/comments")
    public String memberComments(@PathVariable Long id,
                                 @RequestParam(defaultValue = "1") int page,
                                 Model model) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다. id=" + id));

        int pageSize = 10;

        Page<Comment> commentPage =
                commentRepository.findByWriterId(
                        id,
                        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
                );

        List<AdminCommentRow> commentRows = new ArrayList<>();
        for (Comment c : commentPage.getContent()) {
            BasePost post = c.getPost();

            String boardName = post != null ? post.getBoardType() : "-";
            String postTitle = post != null ? post.getTitle() : "-";
            String postUrl = resolvePostUrl(post, c);

            commentRows.add(new AdminCommentRow(
                    c.getContent(),
                    c.getCreatedAt(),
                    boardName,
                    postTitle,
                    postUrl
            ));
        }

        model.addAttribute("comments", commentRows);
        model.addAllAttributes(PageUtil.buildPageModel(commentPage, 5));
        model.addAttribute("memberId", id);
        model.addAttribute("memberDisplayName", member.getDisplayName());

        return "admin/member-comments";
    }

    private String resolvePostUrl(BasePost post, Comment comment) {
        if (post == null) return "#";
        if (comment == null || comment.getId() == null) return "/post/" + post.getId();

        Long commentId = comment.getId();

        Long parentId = null;
        if (comment.getParent() != null) {
            parentId = comment.getParent().getId();
        }

        String openReplyPath = buildOpenReplyPath(comment);

        StringBuilder sb = new StringBuilder();
        sb.append("/post/").append(post.getId());
        sb.append("?commentId=").append(commentId);

        if (parentId != null) {
            sb.append("&parentId=").append(parentId);
        }

        if (openReplyPath != null && !openReplyPath.isBlank()) {
            sb.append("&openReplyPath=").append(openReplyPath);
        }

        sb.append("&focusCommentId=").append(commentId);

        return sb.toString();
    }

    private String buildOpenReplyPath(Comment comment) {
        if (comment == null) {
            return "";
        }

        List<Long> ids = new ArrayList<>();
        Comment p = comment.getParent();

        while (p != null && p.getId() != null) {
            ids.add(p.getId());
            p = p.getParent();
        }

        if (ids.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = ids.size() - 1; i >= 0; i--) {
            sb.append(ids.get(i));
            if (i != 0) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    @Getter
    @AllArgsConstructor
    public static class AdminCommentRow {
        private String content;
        private LocalDateTime createdAt;
        private String boardName;
        private String postTitle;
        private String postUrl;
    }

}
