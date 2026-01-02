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
import com.hello.community.notification.dlt.NotificationDltMessage;
import com.hello.community.notification.dlt.NotificationDltRepository;

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
import org.springframework.transaction.annotation.Transactional;

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
    private final NotificationDltRepository notificationDltRepository;

    // 0. 관리자 - 레거시 페이지네이션 경로 호환
    @GetMapping("/members/page/{page}")
    public String membersLegacy(@PathVariable int page) {
        return "redirect:/admin/members?page=" + page;
    }

    @GetMapping("/members/{page:\\d+}")
    public String membersLegacy2(@PathVariable int page) {
        return "redirect:/admin/members?page=" + page;
    }

    @GetMapping("/member/{id}/{page:\\d+}")
    public String memberCommentsLegacy(@PathVariable Long id,
                                       @PathVariable int page) {
        return "redirect:/admin/member/" + id + "/comments?page=" + page;
    }

    @GetMapping("/member/{id}/comments/page/{page}")
    public String memberCommentsLegacy2(@PathVariable Long id,
                                        @PathVariable int page) {
        return "redirect:/admin/member/" + id + "/comments?page=" + page;
    }

    @GetMapping("/member/{id}/posts/news/page/{page}")
    public String memberPostsNewsLegacy(@PathVariable Long id,
                                        @PathVariable int page) {
        return "redirect:/admin/member/" + id + "/posts?newsPage=" + page + "&musicPage=1&noticePage=1";
    }

    @GetMapping("/member/{id}/posts/music/page/{page}")
    public String memberPostsMusicLegacy(@PathVariable Long id,
                                         @PathVariable int page) {
        return "redirect:/admin/member/" + id + "/posts?newsPage=1&musicPage=" + page + "&noticePage=1";
    }

    @GetMapping("/member/{id}/posts/notice/page/{page}")
    public String memberPostsNoticeLegacy(@PathVariable Long id,
                                          @PathVariable int page) {
        return "redirect:/admin/member/" + id + "/posts?newsPage=1&musicPage=1&noticePage=" + page;
    }

    @GetMapping("/notification-dlt/page/{page}")
    public String notificationDltLegacy(@PathVariable int page) {
        return "redirect:/admin/notification-dlt?page=" + page;
    }

    // 1. 관리자 - 회원 목록 페이지
    @GetMapping("/members")
    public String members(@RequestParam(defaultValue = "1") int page,
                          Model model) {

        int pageSize = 5;

        if (page < 1) {
            page = 1;
        }

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

        int pageSize = 5;

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

        int pageSize = 5;

        if (page < 1) {
            page = 1;
        }

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

    @GetMapping("/notification-dlt")
    public String notificationDlt(@RequestParam(defaultValue = "1") int page,
                                  Model model) {

        int pageSize = 5;

        if (page < 1) {
            page = 1;
        }

        Page<NotificationDltMessage> dltPage =
                notificationDltRepository.findAll(
                        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
                );

        model.addAttribute("items", dltPage.getContent());
        model.addAllAttributes(PageUtil.buildPageModel(dltPage, 5));

        return "admin/notification-dlt";
    }

    @PostMapping("/notification-dlt/{id}/ack")
    @Transactional
    public String notificationDltAck(@PathVariable Long id,
                                     @RequestParam(defaultValue = "1") int page) {

        NotificationDltMessage item = notificationDltRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DLT not found id=" + id));

        item.markAcked();

        return "redirect:/admin/notification-dlt?page=" + page;
    }

    @PostMapping("/notification-dlt/{id}/resolve")
    @Transactional
    public String notificationDltResolve(@PathVariable Long id,
                                         @RequestParam(defaultValue = "1") int page) {

        NotificationDltMessage item = notificationDltRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DLT not found id=" + id));

        item.markResolved();

        return "redirect:/admin/notification-dlt?page=" + page;
    }

    @GetMapping("/notification-dlt/{id}")
    public String notificationDltDetail(@PathVariable Long id,
                                        Model model) {

        NotificationDltMessage item = notificationDltRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DLT not found id=" + id));

        model.addAttribute("item", item);

        return "admin/notification-dlt-detail";
    }

    @PostMapping("/notification-dlt/{id}/ack/detail")
    @Transactional
    public String notificationDltAckDetail(@PathVariable Long id) {

        NotificationDltMessage item = notificationDltRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DLT not found id=" + id));

        item.markAcked();

        return "redirect:/admin/notification-dlt/" + id;
    }

    @PostMapping("/notification-dlt/{id}/resolve/detail")
    @Transactional
    public String notificationDltResolveDetail(@PathVariable Long id) {

        NotificationDltMessage item = notificationDltRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DLT not found id=" + id));

        item.markResolved();

        return "redirect:/admin/notification-dlt/" + id;
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
