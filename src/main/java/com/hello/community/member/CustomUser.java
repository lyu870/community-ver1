// CustomUser.java
package com.hello.community.member;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUser extends User {
    private final Member member;

    public CustomUser(Member member,
                      Collection<? extends GrantedAuthority> authorities) {
        super(member.getUsername(), member.getPassword(), authorities);
        this.member = member;
    }

    // 댓글/게시글 저장 시 사용
    public Member getMember() {
        return member;
    }

    public String getDisplayName() {
        return member.getDisplayName();
    }

    public Long getId() {
        return member.getId();
    }

    public boolean isAdmin() {
        return member.getRole() == Role.ROLE_ADMIN;
    }
}
