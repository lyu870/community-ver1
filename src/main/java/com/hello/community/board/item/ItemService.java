// ItemService.java
package com.hello.community.board.item;

import com.hello.community.board.common.BasePostService;
import com.hello.community.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final BasePostService<Item> itemPostService;
    private final ItemRepository itemRepository;

    @Transactional
    public Item increaseViewCount(Long id) {
        return itemPostService.increaseViewCount(id);
    }

    @Transactional
    public void increaseRecommendCount(Long id) {
        itemPostService.increaseRecommendCount(id);
    }

    @Transactional
    public void decreaseRecommendCount(Long id) {
        itemPostService.decreaseRecommendCount(id);
    }

    @Transactional(readOnly = true)
    public Item findItemById(Long id) {
        return itemPostService.findById(id);
    }

    @Transactional
    public void saveItem(String title, int price, Member writer) {
        Item item = new Item();
        item.setTitle(title);
        item.setPrice(price);
        item.setWriter(writer);
        itemPostService.save(item);
    }

    @Transactional
    public void editItem(Long id, String title, int price) {
        Item item = findItemById(id);
        item.setTitle(title);
        item.setPrice(price);
    }

    @Transactional
    public void deleteItem(Long id, Long loginUserId) {
        // 관리자 정보 없이 호출될 때를 위한 보험.
        deleteItem(id, loginUserId, false);
    }

    @Transactional
    public void deleteItem(Long id, Long loginUserId, boolean isAdmin) {
        // 관리자 여부를 BasePostService 로 전달.
        itemPostService.delete(id, loginUserId, isAdmin);
    }

    @Transactional(readOnly = false)
    public Page<Item> findPage(int num, int pageSize) {
        return itemPostService.findPage(
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }

    @Transactional(readOnly = false)
    public Page<Item> search(String keyword, int num, int pageSize) {
        return itemRepository.searchByKeyword(
                keyword,
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }

    // 자신의 게시글만 조회.
    @Transactional(readOnly = false)
    public Page<Item> findMyPage(Long writerId, int num, int pageSize) {
        return itemRepository.findByWriterId(
                writerId,
                PageRequest.of(
                        num - 1,
                        pageSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }
}
