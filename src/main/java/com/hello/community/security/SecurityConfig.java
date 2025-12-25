// SecurityConfig.java
package com.hello.community.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String LOGIN_REDIRECT_URL = "LOGIN_REDIRECT_URL";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean   // POST + _method=DELETE를 실제 DELETE요청으로 변환
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            RequestCache requestCache = new HttpSessionRequestCache();
            SavedRequest savedRequest = requestCache.getRequest(request, response);

            if (savedRequest != null) {
                response.sendRedirect(savedRequest.getRedirectUrl());
                return;
            }

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            if (httpRequest.getSession(false) != null) {
                Object target = httpRequest.getSession(false).getAttribute(LOGIN_REDIRECT_URL);
                if (target instanceof String targetUrl && !targetUrl.isBlank()) {
                    httpRequest.getSession(false).removeAttribute(LOGIN_REDIRECT_URL);
                    response.sendRedirect(targetUrl);
                    return;
                }
            }

            response.sendRedirect("/");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.addFilterBefore(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

                if ("GET".equals(request.getMethod()) && "/login".equals(request.getRequestURI())) {
                    String referer = request.getHeader("Referer");
                    String targetUrl = getSafeTargetUrl(request, referer);

                    if (targetUrl != null) {
                        request.getSession(true).setAttribute(LOGIN_REDIRECT_URL, targetUrl);
                    }
                }

                filterChain.doFilter(request, response);
            }
        }, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/post/**",
                        "/login",
                        "/register",
                        "/member",

                        "/find-id",
                        "/find-password",

                        "/api/member/check-username",
                        "/api/member/check-displayName",
                        "/api/member/check-email",
                        "/api/member/email-code",
                        "/api/member/email-verify",
                        "/api/member/find-id",
                        "/api/member/find-id/email-code",
                        "/api/member/find-id/confirm",
                        "/api/member/password-reset/email-code",
                        "/api/member/password-reset/verify",
                        "/api/member/password-reset/confirm",

                        // 게시판: 목록/검색/상세페이지 공개
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
                        "/comment/children-fragment",
                        "/api/comment/children"
                ).permitAll()

                // notice게시판의 나머지 URL은 관리자만 사용 가능
                .requestMatchers("/notice/**").hasRole("ADMIN")

                // 댓글 작성은 로그인 필요
                .requestMatchers("/comment", "/comment/**").authenticated()

                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
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

    private String getSafeTargetUrl(HttpServletRequest request, String referer) {
        if (referer == null || referer.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(referer);

            if (uri.getHost() == null) {
                return null;
            }

            if (!request.getServerName().equalsIgnoreCase(uri.getHost())) {
                return null;
            }

            String path = uri.getRawPath();
            if (path == null || path.isBlank()) {
                return null;
            }

            if (path.startsWith("/login") || path.startsWith("/logout")) {
                return null;
            }

            String query = uri.getRawQuery();
            if (query != null && !query.isBlank()) {
                return path + "?" + query;
            }

            return path;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
