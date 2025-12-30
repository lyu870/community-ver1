package com.hello.community.notification.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationItemDto {

    private final Long id;
    private final String type;
    private final String title;
    private final String message;
    private final String linkUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime readAt;
    private final boolean read;

    public NotificationItemDto(Long id,
                               String type,
                               String title,
                               String message,
                               String linkUrl,
                               LocalDateTime createdAt,
                               LocalDateTime readAt,
                               boolean read) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.linkUrl = linkUrl;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.read = read;
    }
}
