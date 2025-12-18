// ChildCommentDto.java
package com.hello.community.comment;

import com.hello.community.comment.Comment;

public class ChildCommentDto {

    private Long id;
    private Long writerId;
    private String writerDisplayName;
    private String content;
    private String createdAt;
    private String updatedAt;
    private long replyCount;

    public ChildCommentDto(Long id,
                           Long writerId,
                           String writerDisplayName,
                           String content,
                           String createdAt,
                           String updatedAt,
                           long replyCount) {
        this.id = id;
        this.writerId = writerId;
        this.writerDisplayName = writerDisplayName;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.replyCount = replyCount;
    }

    // 댓글/게시글 저장 시 사용
    public static ChildCommentDto from(Comment c) {
        Long writerId = (c.getWriter() != null) ? c.getWriter().getId() : null;
        String writerDisplayName = (c.getWriter() != null) ? c.getWriter().getDisplayName() : "";
        String createdAt = (c.getCreatedAt() != null) ? c.getCreatedAt().toString() : null;
        String updatedAt = (c.getUpdatedAt() != null) ? c.getUpdatedAt().toString() : null;

        return new ChildCommentDto(
                c.getId(),
                writerId,
                writerDisplayName,
                c.getContent(),
                createdAt,
                updatedAt,
                c.getReplyCount()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getWriterId() {
        return writerId;
    }

    public String getWriterDisplayName() {
        return writerDisplayName;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public long getReplyCount() {
        return replyCount;
    }
}
