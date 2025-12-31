// NotificationSettingResponseDto.java
package com.hello.community.notification.dto;

public class NotificationSettingResponseDto {

    private boolean postCommentEnabled;
    private boolean commentReplyEnabled;
    private boolean postRecommendEnabled;

    public NotificationSettingResponseDto(boolean postCommentEnabled, boolean commentReplyEnabled, boolean postRecommendEnabled) {
        this.postCommentEnabled = postCommentEnabled;
        this.commentReplyEnabled = commentReplyEnabled;
        this.postRecommendEnabled = postRecommendEnabled;
    }

    public boolean isPostCommentEnabled() {
        return postCommentEnabled;
    }

    public boolean isCommentReplyEnabled() {
        return commentReplyEnabled;
    }

    public boolean isPostRecommendEnabled() {
        return postRecommendEnabled;
    }
}
