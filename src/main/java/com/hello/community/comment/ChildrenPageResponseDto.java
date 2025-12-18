// ChildrenPageResponseDto.java
package com.hello.community.comment;

import java.util.List;

public class ChildrenPageResponseDto {

    private List<com.hello.community.comment.ChildCommentDto> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    public ChildrenPageResponseDto(List<com.hello.community.comment.ChildCommentDto> items,
                                   int page,
                                   int size,
                                   long totalElements,
                                   int totalPages,
                                   boolean hasNext) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }

    public List<com.hello.community.comment.ChildCommentDto> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
