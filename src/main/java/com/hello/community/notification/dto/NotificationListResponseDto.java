// NotificationListResponseDto.java
package com.hello.community.notification.dto;

import java.util.List;

public class NotificationListResponseDto {

    private List<NotificationItemDto> items;
    private boolean hasNext;
    private Integer nextPage;

    public NotificationListResponseDto(List<NotificationItemDto> items, boolean hasNext, Integer nextPage) {
        this.items = items;
        this.hasNext = hasNext;
        this.nextPage = nextPage;
    }

    public List<NotificationItemDto> getItems() {
        return items;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public Integer getNextPage() {
        return nextPage;
    }
}
