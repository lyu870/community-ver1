// ItemController.java
package com.hello.community.board.item;

import com.hello.community.board.common.PageUtil;
import com.hello.community.board.recommend.PostRecommendService;
import com.hello.community.board.recommend.RecommendResponseDto;
import com.hello.community.comment.CommentRepository;
import com.hello.community.comment.CommentService;
import com.hello.community.member.CustomUser;
import com.hello.community.member.Member;
import com.hello.community.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/item")
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemService itemService;
    private final CommentService commentService;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final PostRecommendService postRecommendService;

    @GetMapping("/list")
    String list(Model model){
        return "redirect:/item/list/page/1";
    }

    @GetMapping("/write")
    String write(Model model) {
        return "board/item/write";
    }

    // detail페이지
    @GetMapping("/detail/{id}")
    String detail(@PathVariable Long id,
                  Model model,
                  @AuthenticationPrincipal CustomUser user) {

        Item item = itemService.increaseViewCount(id);

        model.addAttribute("data", item);
        model.addAttribute("comments", commentService.getComment(id));

        Long loginUserId = (user != null) ? user.getId() : null;
        model.addAttribute("loginUserId", loginUserId);

        boolean isRecommended = false;
        if (user != null) {
            isRecommended = postRecommendService.isRecommended(id, user.getId());
        }
        model.addAttribute("isRecommended", isRecommended);

        boolean isAdmin = user != null && user.isAdmin();
        model.addAttribute("isAdmin", isAdmin);

        return "board/item/detail";
    }

    // 게시글 작성 요청
    @PostMapping("/add")
    String writePost(@RequestParam String title,
                     @RequestParam String price,
                     @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        try {
            int p = Integer.parseInt(price.trim());
            if (p < 0) {
                throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
            }

            Member writer = memberRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

            itemService.saveItem(title, p, writer);

            return "redirect:/item/list/page/1";

        } catch (NumberFormatException e) {
            return "redirect:/item/write?error=price";
        }
    }

    // 게시글 수정 요청
    @GetMapping("/edit/{id}")
    String edit(@PathVariable Long id,
                @AuthenticationPrincipal CustomUser user,
                Model model) {

        if (user == null) return "redirect:/login";

        Optional<Item> result = itemRepository.findById(id);
        if (result.isEmpty()) return "redirect:/item/list/page/1";

        Item item = result.get();
        if (!item.getWriter().getId().equals(user.getId())) {
            return "redirect:/item/detail/" + id;
        }

        model.addAttribute("data", item);
        return "board/item/edit";
    }

    // 게시글 수정 완료요청
    @PostMapping("/edit")
    public String editPost(@RequestParam Long id,
                           @RequestParam String title,
                           @RequestParam Integer price,
                           @AuthenticationPrincipal CustomUser user) {

        if (user == null) return "redirect:/login";

        itemService.editItem(id, title, price);
        return "redirect:/item/detail/" + id;
    }

    // 게시글 삭제 요청
    @DeleteMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        boolean isAdmin = user.isAdmin();

        // 관리자/작성자 여부를 함께 넘기고 BasePostService 에서 권한 체크
        itemService.deleteItem(id, user.getId(), isAdmin);

        return "redirect:/item/list/page/1";
    }

    // list.html 페이지네이션
    @GetMapping("/list/page/{num}")
    String getListPage(Model model,
                       @PathVariable Integer num,
                       @AuthenticationPrincipal CustomUser user) {
        int pageSize = 10;

        Page<Item> page = itemService.findPage(num, pageSize);
        List<Item> items = page.getContent();

        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(page, 5));
        model.addAttribute("searchMode", false);

        // 로그인 사용자 / 내글 모드 여부
        Long loginUserId = (user != null) ? user.getId() : null;
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("myMode", false);

        Map<Long, Long> commentCounts = items.stream()
                .collect(Collectors.toMap(
                        Item::getId,
                        post -> commentRepository.countByPost(post)
                ));
        model.addAttribute("commentCounts", commentCounts);

        return "board/item/list";
    }

    // 내가 쓴 글 목록 페이지
    @GetMapping("/my/page/{num}")
    String getMyListPage(Model model,
                         @PathVariable Integer num,
                         @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return "redirect:/login";
        }

        int pageSize = 10;

        Page<Item> page = itemService.findMyPage(user.getId(), num, pageSize);
        List<Item> items = page.getContent();

        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(page, 5));
        model.addAttribute("searchMode", false);
        model.addAttribute("myMode", true);
        model.addAttribute("loginUserId", user.getId());

        Map<Long, Long> commentCounts = items.stream()
                .collect(Collectors.toMap(
                        Item::getId,
                        post -> commentRepository.countByPost(post)
                ));
        model.addAttribute("commentCounts", commentCounts);

        return "board/item/list";
    }

    // 검색기능
    @PostMapping("/search")
    String postSearch(@RequestParam String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return "redirect:/item/list/page/1";
        }
        String q = URLEncoder.encode(searchText.trim(), StandardCharsets.UTF_8);
        return "redirect:/item/search/page/1?q=" + q;
    }

    // 검색결과 페이지네이션
    @GetMapping("/search/page/{num}")
    String getSearchPage(@PathVariable Integer num,
                         @RequestParam(name = "q") String q,
                         Model model) {

        int pageSize = 10;
        int pageLinkCount = 5;

        Page<Item> page =
                itemRepository.searchByKeyword(q, PageRequest.of(num - 1, pageSize));

        List<Item> items = page.getContent();
        model.addAttribute("items", items);
        model.addAllAttributes(PageUtil.buildPageModel(page, pageLinkCount));
        model.addAttribute("searchMode", true);
        model.addAttribute("query", q);
        model.addAttribute("count", page.getTotalElements());

        // 댓글수·조회수·추천수
        Map<Long, Long> commentCounts = items.stream()
                .collect(Collectors.toMap(
                        Item::getId,
                        post -> commentRepository.countByPost(post)
                ));
        model.addAttribute("commentCounts", commentCounts);

        return "board/item/list";
    }

    // 추천 토글 API (로그인 필요)
    @PostMapping("/{id}/recommend")
    @ResponseBody
    public ResponseEntity<RecommendResponseDto> toggleRecommend(@PathVariable Long id,
                                                                @AuthenticationPrincipal CustomUser user) {

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        boolean recommended = postRecommendService.toggleRecommend(id, user.getId());

        if (recommended) {
            itemService.increaseRecommendCount(id);
        } else {
            itemService.decreaseRecommendCount(id);
        }

        Item updated = itemService.findItemById(id);

        RecommendResponseDto body = new RecommendResponseDto(
                recommended,
                updated.getRecommendCount()
        );

        return ResponseEntity.ok(body);
    }
}
