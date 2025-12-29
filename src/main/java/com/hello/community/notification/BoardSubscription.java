// BoardSubscription.java
package com.hello.community.notification;

import com.hello.community.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "board_subscription",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_board_subscription_member_board", columnNames = {"member_id", "board_type"})
        },
        indexes = {
                @Index(name = "idx_board_subscription_board_enabled", columnList = "board_type, enabled"),
                @Index(name = "idx_board_subscription_member", columnList = "member_id")
        }
)
@Getter
@NoArgsConstructor
public class BoardSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false, length = 30)
    private BoardType boardType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static BoardSubscription of(Member member, BoardType boardType, boolean enabled) {
        BoardSubscription s = new BoardSubscription();
        s.member = member;
        s.boardType = boardType;
        s.enabled = enabled;
        s.createdAt = LocalDateTime.now();
        s.updatedAt = s.createdAt;
        return s;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
