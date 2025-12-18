// SecurityConfig.java
package com.hello.community.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean   // POST + _method=DELETE를 실제 DELETE요청으로 변환
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/member",
                        "/member/check-username",
                        "/member/check-displayName",
                        "/member/check-email",
                        "/member/email-code",
                        "/member/email-verify",
                        "/member/find-id",
                        "/member/find-pw",
                        "/find-id",
                        "/find-password",
                        "/member/find-id/email-code",
                        "/member/find-id/confirm",
                        "/member/password-reset/email-code",
                        "/member/password-reset/verify",
                        "/member/password-reset/confirm",

                        // 게시판: 목록/검색/상세페이지 공개
                        "/item/list",
                        "/item/list/**",
                        "/item/search",
                        "/item/search/**",
                        "/item/detail/**",

                        "/notice/list",
                        "/notice/list/**",
                        "/notice/search",
                        "/notice/search/**",
                        "/notice/detail/**",

                        "/music/list",
                        "/music/list/**",
                        "/music/search",
                        "/music/search/**",
                        "/music/detail/**",

                        "/news/list",
                        "/news/list/**",
                        "/news/search",
                        "/news/search/**",
                        "/news/detail/**",

                        "/logout-success",
                        "/member/logout-success",

                        "/main.css",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**"
                ).permitAll()

                // 답글 조회 비로그인도 가능
                .requestMatchers(HttpMethod.GET,
                        "/api/comment/children",
                        "/api/children",
                        "/comment/children",
                        "/comment/children-fragment",
                        "/children"
                ).permitAll()

                // notice게시판의 나머지 URL은 관리자만 사용 가능
                .requestMatchers("/notice/**").hasRole("ADMIN")

                // item 글쓰기/등록은 관리자만 사용 가능
                .requestMatchers("/item/write", "/item/add").hasRole("ADMIN")

                // 댓글 작성은 로그인 필요
                .requestMatchers("/comment", "/comment/**").authenticated()

                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        return http.build();
    }
}
