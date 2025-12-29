// ReadNotificationsResponseDto.java
package com.hello.community.notification.dto;

public class ReadNotificationsResponseDto {

    private int updated;

    public ReadNotificationsResponseDto(int updated) {
        this.updated = updated;
    }

    public int getUpdated() {
        return updated;
    }
}
