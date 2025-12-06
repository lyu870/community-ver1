// SalesController.java
package com.hello.community.sales;

import com.hello.community.board.item.ItemRepository;
import com.hello.community.member.CustomUser;
import com.hello.community.member.Member;
import com.hello.community.sales.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SalesController {
    private final SalesRepository salesRepository;
    private final ItemRepository itemRepository;

    @PostMapping("/order")
    String postOrder(@RequestParam Long itemId,
                     @RequestParam Integer count,
                     @AuthenticationPrincipal CustomUser user) { // Authentication으로 받지말고 사용자 바로 주입
        // 1) 상품 조회
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. id=" + itemId));

        // 2) 주문 엔티티 생성
        Sales sales = new Sales();
        sales.setItemName(item.getTitle()); // 폼에서 안받고 DB에서 채우기 (결제 시 개발자도구로 가격조작 방어)
        sales.setPrice(item.getPrice()); // 현재 판매가를 스냅샷으로 기록
        sales.setCount(count);

        // 3) 회원 참조 세팅 (프록시로 id만 넣어도 됨)
        Member ref = new Member();
        ref.setId(user.getId()); // CustomUser에 getId() 있어야함
        sales.setMember(ref);

        salesRepository.save(sales);

        return "redirect:/detail/" + itemId;
    }

    @GetMapping("/order/all")
    String orderList(Model model) {
        List<Sales> list = salesRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("orders", list);
        return "sales.html";
    }
}
