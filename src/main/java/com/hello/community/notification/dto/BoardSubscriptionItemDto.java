// BoardSubscriptionItemDto.java
package com.hello.community.notification.dto;

public class BoardSubscriptionItemDto {

    private String boardType;
    private boolean enabled;

    public BoardSubscriptionItemDto(String boardType, boolean enabled) {
        this.boardType = boardType;
        this.enabled = enabled;
    }

    public String getBoardType() {
        return boardType;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
