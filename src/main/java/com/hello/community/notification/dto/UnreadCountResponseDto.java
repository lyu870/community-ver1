// UnreadCountResponseDto.java
package com.hello.community.notification.dto;

public class UnreadCountResponseDto {

    private long unreadCount;

    public UnreadCountResponseDto(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getUnreadCount() {
        return unreadCount;
    }
}
