// NotificationListResponseDto.java
package com.hello.community.notification.dto;

import java.util.List;

public class NotificationListResponseDto {

    private List<NotificationItemDto> items;

    public NotificationListResponseDto(List<NotificationItemDto> items) {
        this.items = items;
    }

    public List<NotificationItemDto> getItems() {
        return items;
    }
}
