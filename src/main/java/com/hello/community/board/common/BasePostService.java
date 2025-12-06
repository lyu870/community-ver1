// BasePostService.java
package com.hello.community.board.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class BasePostService<T extends BasePost> {

    private final BasePostRepository<T> repository;

    @Transactional
    public T increaseViewCount(Long id) {
        T post = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        post.setViewCount(post.getViewCount() + 1);
        return post;
    }

    @Transactional
    public void increaseRecommendCount(Long id) {
        repository.findById(id).ifPresent(post ->
                post.setRecommendCount(post.getRecommendCount() + 1)
        );
    }

    @Transactional
    public void decreaseRecommendCount(Long id) {
        repository.findById(id).ifPresent(post ->
                post.setRecommendCount(Math.max(0, post.getRecommendCount() - 1))
        );
    }

    @Transactional(readOnly = true)
    public T findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
    }

    @Transactional
    public T save(T post) {
        return repository.save(post);
    }

    @Transactional
    public void delete(Long id, Long loginUserId) {
        // 관리자 여부 정보를 받지 못하는 기존 호출은 일반 사용자로 간주.
        delete(id, loginUserId, false);
    }

    @Transactional
    public void delete(Long id, Long loginUserId, boolean isAdmin) {
        // 관리자이거나 작성자인 경우에만 삭제 허용
        T post = findById(id);
        boolean isOwner = post.getWriter().getId().equals(loginUserId);
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        repository.delete(post);
    }

    @Transactional(readOnly = false)
    public Page<T> findPage(Pageable pageable) {

        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return repository.findAll(sorted);
    }
}
