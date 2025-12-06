// PostRecommend.java
package com.hello.community.board.recommend;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_recommend",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_recommend_post_member",
                        columnNames = {"post_id", "member_id"}
                )
        }
)
@Getter
@Setter
public class PostRecommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글을 추천했는가 (BasePost의 id)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    // 어떤 회원이 추천했는가 (Member의 id)
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}