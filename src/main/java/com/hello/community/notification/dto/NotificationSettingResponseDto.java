// NotificationSettingResponseDto.java
package com.hello.community.notification.dto;

public class NotificationSettingResponseDto {

    private boolean postCommentEnabled;
    private boolean commentReplyEnabled;

    public NotificationSettingResponseDto(boolean postCommentEnabled, boolean commentReplyEnabled) {
        this.postCommentEnabled = postCommentEnabled;
        this.commentReplyEnabled = commentReplyEnabled;
    }

    public boolean isPostCommentEnabled() {
        return postCommentEnabled;
    }

    public boolean isCommentReplyEnabled() {
        return commentReplyEnabled;
    }
}
