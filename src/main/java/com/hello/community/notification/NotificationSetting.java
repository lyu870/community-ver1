// NotificationSetting.java
package com.hello.community.notification;

import com.hello.community.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_setting")
@Getter
@NoArgsConstructor
public class NotificationSetting implements Persistable<Long> {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "post_comment_enabled", nullable = false)
    private boolean postCommentEnabled = true;

    @Column(name = "comment_reply_enabled", nullable = false)
    private boolean commentReplyEnabled = true;

    @Column(name = "post_recommend_enabled", nullable = false)
    private boolean postRecommendEnabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew = false;

    @Override
    public Long getId() {
        return memberId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    private void markNotNew() {
        this.isNew = false;
    }

    @PrePersist
    public void prePersist() {
        this.isNew = true;

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static NotificationSetting of(Member member) {
        NotificationSetting s = new NotificationSetting();
        s.member = member;
        s.memberId = member.getId();
        s.postCommentEnabled = true;
        s.commentReplyEnabled = true;
        s.postRecommendEnabled = true;
        s.createdAt = LocalDateTime.now();
        s.updatedAt = s.createdAt;
        s.isNew = true;
        return s;
    }

    public void update(Boolean postCommentEnabled, Boolean commentReplyEnabled, Boolean postRecommendEnabled) {
        if (postCommentEnabled != null) {
            this.postCommentEnabled = postCommentEnabled;
        }
        if (commentReplyEnabled != null) {
            this.commentReplyEnabled = commentReplyEnabled;
        }
        if (postRecommendEnabled != null) {
            this.postRecommendEnabled = postRecommendEnabled;
        }
    }
}
