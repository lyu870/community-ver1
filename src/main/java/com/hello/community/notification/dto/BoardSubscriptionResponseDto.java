// BoardSubscriptionResponseDto.java
package com.hello.community.notification.dto;

import java.util.List;

public class BoardSubscriptionResponseDto {

    private List<BoardSubscriptionItemDto> items;

    public BoardSubscriptionResponseDto(List<BoardSubscriptionItemDto> items) {
        this.items = items;
    }

    public List<BoardSubscriptionItemDto> getItems() {
        return items;
    }
}
