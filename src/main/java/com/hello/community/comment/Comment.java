// Comment.java
package com.hello.community.comment;

import com.hello.community.board.common.BasePost;
import com.hello.community.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(length = 1000)
    public String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private BasePost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private Member writer;

    // 부모 댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 자식 댓글 리스트
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Comment> children = new ArrayList<>();

    // 댓글 생성 메서드 (편의성) → 생성때마다 setPost(), setWriter()호출 안하게해줌.
    public static Comment create(BasePost post, Member writer, String content, Comment parent) {
        Comment c = new Comment();
        c.setPost(post);
        c.setWriter(writer);
        c.setContent(content);
        c.setParent(parent);
        return c;
    }
}
