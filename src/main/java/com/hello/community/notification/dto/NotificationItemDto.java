// NotificationItemDto.java
package com.hello.community.notification.dto;

import java.time.LocalDateTime;

public class NotificationItemDto {

    private Long id;
    private String type;
    private String message;
    private String linkUrl;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private boolean read;

    public NotificationItemDto(Long id, String type, String message, String linkUrl, LocalDateTime createdAt, LocalDateTime readAt, boolean read) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.linkUrl = linkUrl;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.read = read;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public boolean isRead() {
        return read;
    }
}
