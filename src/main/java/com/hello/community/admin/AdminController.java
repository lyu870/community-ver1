// AdminController.java
package com.hello.community.admin;

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

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
                              @RequestParam(defaultValue = "1") int page,
                              Model model) {

        int pageSize = 10;

        Page<News> news = newsRepository.findByWriterId(id, PageRequest.of(page - 1, pageSize));
        Page<Music> music = musicRepository.findByWriterId(id, PageRequest.of(page - 1, pageSize));
        Page<Notice> notice = noticeRepository.findByWriterId(id, PageRequest.of(page - 1, pageSize));

        model.addAttribute("news", news.getContent());
        model.addAttribute("music", music.getContent());
        model.addAttribute("notice", notice.getContent());

        model.addAttribute("memberId", id);

        return "admin/member-posts";
    }

    // 3. 관리자 - 특정 회원의 댓글 전체
    @GetMapping("/member/{id}/comments")
    public String memberComments(@PathVariable Long id,
                                 @RequestParam(defaultValue = "1") int page,
                                 Model model) {

        int pageSize = 10;

        Page<Comment> commentPage =
                commentRepository.findByWriterId(id, PageRequest.of(page - 1, pageSize));

        model.addAttribute("comments", commentPage.getContent());
        model.addAllAttributes(PageUtil.buildPageModel(commentPage, 5));
        model.addAttribute("memberId", id);

        return "admin/member-comments";
    }

}
