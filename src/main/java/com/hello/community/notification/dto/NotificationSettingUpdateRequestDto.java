// NotificationSettingUpdateRequestDto.java
package com.hello.community.notification.dto;

public class NotificationSettingUpdateRequestDto {

    private Boolean postCommentEnabled;
    private Boolean commentReplyEnabled;

    public Boolean getPostCommentEnabled() {
        return postCommentEnabled;
    }

    public void setPostCommentEnabled(Boolean postCommentEnabled) {
        this.postCommentEnabled = postCommentEnabled;
    }

    public Boolean getCommentReplyEnabled() {
        return commentReplyEnabled;
    }

    public void setCommentReplyEnabled(Boolean commentReplyEnabled) {
        this.commentReplyEnabled = commentReplyEnabled;
    }
}
