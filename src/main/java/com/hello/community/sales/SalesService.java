// SalesService.java
package com.hello.community.sales;

import com.hello.community.board.item.ItemRepository;
import com.hello.community.member.MemberRepository;
import com.hello.community.sales.Sales;
import com.hello.community.sales.SalesOrderRequest;
import com.hello.community.sales.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalesService {
    private final SalesRepository salesRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long placeOrder(Long memberId, SalesOrderRequest req) {
        // 엔티티 프록시 가져오기(실제 조회는 필요할 때 수행)
        var memberRef = memberRepository.getReferenceById(memberId);
        var itemRef   = itemRepository.getReferenceById(req.getItemId());

        // 가격/상품명은 서버에서 결정 (신뢰할 수 있는 단일 소스)
        var price = itemRef.getPrice();
        var title = itemRef.getTitle();

        var sales = new Sales();
        sales.setItemName(title);
        sales.setPrice(price);
        sales.setCount(req.getCount());
        sales.setMember(memberRef);

        salesRepository.save(sales);
        return sales.getId();
    }
}
