// BasePost.java
package com.hello.community.board.common;

import com.hello.community.member.Member;
import com.hello.community.comment.Comment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "base_post",
        indexes = {
                @Index(name = "idx_base_post_title", columnList = "title")
        }
)
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter
public abstract class BasePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content", length = 5000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private Member writer;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "view_count")
    private int viewCount = 0;

    @Column(name = "recommend_count")
    private int recommendCount = 0;

    // 게시글이 가진 댓글 목록 (부모 → 자식 방향)
    @OneToMany(mappedBy = "post",
            cascade = CascadeType.REMOVE, // 게시글 삭제 시 해당 게시글관련 댓글까지 모두 삭제시킴.
            orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
