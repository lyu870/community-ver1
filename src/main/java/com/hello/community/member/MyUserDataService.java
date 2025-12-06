// MyUserDataService.java
package com.hello.community.member;

import com.hello.community.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyUserDataService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("그런 아이디 없음"));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Member 엔티티의 Role값 사용
        authorities.add(new SimpleGrantedAuthority(member.getRole().name()));

        return new CustomUser(
                member,   // 반드시 인코딩된 비번
                authorities
        );
    }
}
