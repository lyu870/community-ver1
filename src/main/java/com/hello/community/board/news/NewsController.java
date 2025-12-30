// NewsController.java
package com.hello.community.board.news;

import com.hello.community.board.common.PageUtil;
import com.hello.community.board.recommend.PostRecommendService;
import com.hello.community.comment.CommentRepository;
import com.hello.community.comment.CommentService;
import com.hello.community.member.CustomUser;
import com.hello.community.member.Member;
import com.hello.community.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;
    private final CommentService commentService;
    private final MemberRepository memberRepository;
    private final PostRecommendService postRecommendService;
    private final CommentRepository commentRepository;

    @GetMapping("/list")
    public String list() {
        return "redirect:/news/list/page/1";
    }

    @GetMapping("/list/page/{num}")
    public String listPage(@PathVariable int num,
                           Model model,
                           @AuthenticationPrincipal CustomUser user) {

        int pageSize = 10;
        Page<News> page = newsService.findPage(num, pageSize);

        List<News> items = page.getContent();

        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(page, 5));
        model.addAttribute("searchMode", false);

        // 로그인 사용자 / 내글 모드
        Long loginUserId = (user != null) ? user.getId() : null;
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("myMode", false);

        // 댓글수·조회수·추천수
        Map<Long, Long> commentCounts = items.stream()
                .collect(Collectors.toMap(
                        News::getId,
                        post -> commentRepository.countByPost(post)
                ));
        model.addAttribute("commentCounts", commentCounts);

        return "board/news/list";
    }

    // 내가 쓴 게시글 목록
    @GetMapping("/my/page/{num}")
    public String myListPage(@PathVariable int num,
                             Model model,
                             @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        int pageSize = 10;
        Page<News> page = newsService.findMyPage(user.getId(), num, pageSize);

        List<News> items = page.getContent();

        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(page, 5));
        model.addAttribute("searchMode", false);
        model.addAttribute("myMode", true);
        model.addAttribute("loginUserId", user.getId());

        Map<Long, Long> commentCounts = items.stream()
                .collect(Collectors.toMap(
                        News::getId,
                        post -> commentRepository.countByPost(post)
                ));
        model.addAttribute("commentCounts", commentCounts);

        return "board/news/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         @AuthenticationPrincipal CustomUser user) {

        News news = newsService.increaseViewCount(id);

        model.addAttribute("data", news);
        model.addAttribute("comments", commentService.getComment(id));
        model.addAttribute("enableBoardDetailJs", true);

        Long loginUserId = (user != null) ? user.getId() : null;
        model.addAttribute("loginUserId", loginUserId);

        boolean recommended = false;
        if (loginUserId != null) {
            recommended = postRecommendService.isRecommended(id, loginUserId);
        }
        model.addAttribute("recommended", recommended);

        boolean isAdmin = user != null && user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        return "board/news/detail";
    }

    @GetMapping("/write")
    public String writeForm() {
        return "board/news/write";
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

        newsService.saveNews(title, content, writer);
        return "redirect:/news/list/page/1";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUser user,
                           Model model) {

        if (user == null) {
            return "redirect:/login";
        }

        News news = newsService.findById(id);
        if (!news.getWriter().getId().equals(user.getId())) {
            return "redirect:/news/detail/" + id;
        }

        model.addAttribute("data", news);
        return "board/news/edit";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam Long id,
                       @RequestParam String title,
                       @RequestParam String content,
                       @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        newsService.editNews(id, title, content, user.getId());
        return "redirect:/news/detail/" + id;
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        newsService.delete(id, user.getId());
        return "redirect:/news/list/page/1";
    }

    // 검색 기능 (GET/POST 모두 허용: GET 기준으로 통일)
    @RequestMapping(value = "/search", method = {RequestMethod.GET, RequestMethod.POST})
    public String postSearch(@RequestParam(required = false) String searchText,
                             @RequestParam(required = false, name = "q") String q) {

        String keyword = (q != null) ? q : searchText;

        if (keyword == null || keyword.isBlank()) {
            return "redirect:/news/list/page/1";
        }

        String encoded = URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
        return "redirect:/news/search/page/1?q=" + encoded;
    }

    // 검색 페이지네이션
    @GetMapping("/search/page/{num}")
    public String getSearchPage(@PathVariable int num,
                                @RequestParam(name = "q") String q,
                                Model model,
                                @AuthenticationPrincipal CustomUser user) {

        int pageSize = 10;
        int pageLinkCount = 5;

        Page<News> page =
                newsService.search(q, num, pageSize);

        List<News> items = page.getContent();

        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(page, pageLinkCount));
        model.addAttribute("searchMode", true);
        model.addAttribute("query", q);

        // 댓글수
        Map<Long, Long> commentCounts = items.stream()
                .collect(Collectors.toMap(
                        News::getId,
                        post -> commentRepository.countByPost(post)
                ));
        model.addAttribute("commentCounts", commentCounts);

        // 검색 모드에서도 버튼 상태 유지
        Long loginUserId = (user != null) ? user.getId() : null;
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("myMode", false);

        return "board/news/list";
    }
}
