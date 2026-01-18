// NoticeController.java
package com.hello.community.board.notice;

import com.hello.community.board.common.PageUtil;
import com.hello.community.board.common.YouTubeEmbedUtil;
import com.hello.community.member.CustomUser;
import com.hello.community.member.Member;
import com.hello.community.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/list")
    public String list() {
        return "redirect:/notice/list/page/1";
    }

    @GetMapping("/list/page/{num}")
    public String listPage(@PathVariable int num,
                           Model model,
                           @AuthenticationPrincipal CustomUser user) {

        int pageSize = 10;
        Page<Notice> page = noticeService.findPage(num, pageSize);

        List<Notice> items = page.getContent();

        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(page, 5));
        model.addAttribute("searchMode", false);

        // 로그인 사용자 / 내글 모드
        Long loginUserId = (user != null) ? user.getId() : null;
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("myMode", false);

        return "board/notice/list";
    }

    // 내가 쓴 공지 목록
    @GetMapping("/my/page/{num}")
    public String myListPage(@PathVariable int num,
                             Model model,
                             @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        int pageSize = 10;
        Page<Notice> page = noticeService.findMyPage(user.getId(), num, pageSize);

        List<Notice> items = page.getContent();

        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(page, 5));
        model.addAttribute("searchMode", false);
        model.addAttribute("myMode", true);
        model.addAttribute("loginUserId", user.getId());

        return "board/notice/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         @AuthenticationPrincipal CustomUser user) {

        Notice notice = noticeService.increaseViewCount(id);

        model.addAttribute("data", notice);
        model.addAttribute("youtubeEmbedUrl", YouTubeEmbedUtil.extractEmbedUrl(notice.getContent()));
        model.addAttribute("contentHtml", YouTubeEmbedUtil.renderContentHtml(notice.getContent()));
        model.addAttribute("enableBoardDetailJs", true);

        Long loginUserId = (user != null) ? user.getId() : null;
        model.addAttribute("loginUserId", loginUserId);

        boolean isAdmin = user != null && user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        return "board/notice/detail";
    }

    @GetMapping("/write")
    public String writeForm() {
        return "board/notice/write";
    }

    @PostMapping("/write")
    public String add(@RequestParam String title,
                      @RequestParam String content,
                      @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        Member writer = memberRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Notice saved = noticeService.saveNotice(title, content, writer);
        return "redirect:/post/" + saved.getId();
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUser user,
                           Model model) {

        if (user == null) {
            return "redirect:/login";
        }

        Notice notice = noticeService.findById(id);
        if (!notice.getWriter().getId().equals(user.getId())) {
            return "redirect:/notice/detail/" + id;
        }

        model.addAttribute("data", notice);
        return "board/notice/edit";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam Long id,
                       @RequestParam String title,
                       @RequestParam String content,
                       @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        noticeService.editNotice(id, title, content, user.getId());
        return "redirect:/notice/detail/" + id;
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        noticeService.delete(id, user.getId(), isAdmin);
        return "redirect:/notice/list/page/1";
    }

    // 검색 기능 (GET/POST 모두 허용: GET 기준으로 통일)
    @RequestMapping(value = "/search", method = {RequestMethod.GET, RequestMethod.POST})
    public String postSearch(@RequestParam(required = false) String searchText,
                             @RequestParam(required = false, name = "q") String q) {

        String keyword = (q != null) ? q : searchText;

        if (keyword == null || keyword.isBlank()) {
            return "redirect:/notice/list/page/1";
        }

        String encoded = URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
        return "redirect:/notice/search/page/1?q=" + encoded;
    }

    // 검색 페이지네이션
    @GetMapping("/search/page/{num}")
    public String getSearchPage(@PathVariable int num,
                                @RequestParam("q") String q,
                                Model model,
                                @AuthenticationPrincipal CustomUser user) {

        int pageSize = 10;
        Page<Notice> page = noticeRepository.searchByKeyword(
                q,
                PageRequest.of(num - 1, pageSize)
        );

        List<Notice> items = page.getContent();

        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(page, 5));
        model.addAttribute("searchMode", true);
        model.addAttribute("query", q);

        // 검색 모드에서도 버튼 상태 유지
        Long loginUserId = (user != null) ? user.getId() : null;
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("myMode", false);

        return "board/notice/list";
    }

}
