package com.hello.community.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String LOGIN_REDIRECT_URL = "LOGIN_REDIRECT_URL";

    @Value("${app.security.csrf.enabled:false}")
    private boolean csrfEnabled;

    private static final RequestMatcher LOGOUT_POST_MATCHER = request -> {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        return "/logout".equals(uri) || "/logout/".equals(uri);
    };

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
                String redirectUrl = savedRequest.getRedirectUrl();
                if (isSafeRedirectUrl(request, redirectUrl)) {
                    response.sendRedirect(redirectUrl);
                    return;
                }
            }

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            if (httpRequest.getSession(false) != null) {
                Object target = httpRequest.getSession(false).getAttribute(LOGIN_REDIRECT_URL);
                if (target instanceof String targetUrl && !targetUrl.isBlank()) {
                    httpRequest.getSession(false).removeAttribute(LOGIN_REDIRECT_URL);

                    if (isSafeRedirectPath(targetUrl)) {
                        response.sendRedirect(targetUrl);
                        return;
                    }
                }
            }

            response.sendRedirect("/");
        };
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/api/**");

        if (!csrfEnabled) {
            http.csrf(csrf -> csrf.disable());
        }

        // API 요청은 인증 실패/권한 없음 시 로그인 redirect 대신 JSON 응답
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":null,\"errorCode\":null,\"error\":\"로그인이 필요합니다.\",\"data\":null}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":null,\"errorCode\":null,\"error\":\"접근 권한이 없습니다.\",\"data\":null}");
                })
        );

        http.authorizeHttpRequests(auth -> auth

                .requestMatchers(
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
                        "/api/member/password-reset/confirm"
                ).permitAll()

                // 답글 조회 비로그인도 가능
                .requestMatchers(HttpMethod.GET,
                        "/api/comment/children"
                ).permitAll()

                // 알림/설정 API는 로그인 필요
                .requestMatchers("/api/notifications/**").authenticated()
                .requestMatchers("/api/notification-settings/**").authenticated()
                .requestMatchers("/api/board-subscriptions/**").authenticated()

                // 댓글 API 중 쓰기/수정/삭제는 로그인 필요
                .requestMatchers(HttpMethod.POST, "/api/comment", "/api/comment/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/comment/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/comment/**").authenticated()

                // 추천 API는 로그인 필요
                .requestMatchers("/api/recommend/**", "/api/post-recommend/**").authenticated()

                // 그 외 /api/member/** (위에서 permitAll로 열어둔 항목 제외) 는 로그인 필요
                .requestMatchers("/api/member/**").authenticated()

                // 없는 API 경로도 DispatcherServlet까지 가서 404(JSON) 떨어지게 허용
                .anyRequest().permitAll()
        );

        http.formLogin(form -> form.disable());
        http.logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        if (!csrfEnabled) {
            http.csrf(csrf -> csrf.disable());
        } else {
            http.csrf(csrf -> csrf
                    // 로그아웃은 토큰 누락 시에도 403 없이 동작하도록 예외 처리
                    .ignoringRequestMatchers(LOGOUT_POST_MATCHER)
            );
        }

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

                // 에러/포워드 디스패치가 인증에 막히지 않도록 허용
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                .requestMatchers("/error", "/error/**").permitAll()

                // GET /logout은 컨트롤러로 흘려보내서 redirect 처리
                .requestMatchers(HttpMethod.GET, "/logout", "/logout/").permitAll()

                // 관리자 페이지는 관리자만
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 공지사항: 작성/수정/삭제만 관리자
                .requestMatchers(
                        "/notice/write",
                        "/notice/add",
                        "/notice/edit",
                        "/notice/edit/**",
                        "/notice/update/**",
                        "/notice/delete",
                        "/notice/delete/**"
                ).hasRole("ADMIN")

                // 댓글 작성은 로그인 필요
                .requestMatchers("/comment", "/comment/**").authenticated()

                // 게시판 글쓰기/수정/삭제는 로그인 필요
                .requestMatchers(
                        "/music/write",
                        "/music/add",
                        "/music/edit/**",
                        "/music/update/**",
                        "/music/delete/**",
                        "/news/write",
                        "/news/add",
                        "/news/edit/**",
                        "/news/update/**",
                        "/news/delete/**"
                ).authenticated()

                // 마이페이지/주문 등은 로그인 필요 (프로젝트에 해당 경로가 없다면 영향 없음)
                .requestMatchers(
                        "/my-page",
                        "/withdraw",
                        "/order",
                        "/order/**"
                ).authenticated()

                .requestMatchers(
                        "/",
                        "/post/**",
                        "/login",
                        "/register",
                        "/member",

                        "/find-id",
                        "/find-password",

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

                        "/.well-known/**",

                        "/main.css",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**"
                ).permitAll()

                .requestMatchers(HttpMethod.GET,
                        "/actuator/health",
                        "/actuator/health/**"
                ).permitAll()

                .requestMatchers("/actuator/**").denyAll()

                // 답글 조회 비로그인도 가능
                .requestMatchers(HttpMethod.GET,
                        "/comment/children-fragment"
                ).permitAll()

                // 비로그인으로 없는경로 진입 시 로그인으로 튀지 않고 404로 처리되게 GET은 기본 허용
                .requestMatchers(HttpMethod.GET, "/**").permitAll()

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
                // 로그아웃은 POST만 허용 (GET /logout 접근 시 403으로 떨어지는 문제 방지)
                .logoutRequestMatcher(LOGOUT_POST_MATCHER)
                .logoutSuccessHandler((request, response, authentication) -> {

                    if (isAjaxRequest(request)) {
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        return;
                    }

                    String referer = request.getHeader("Referer");
                    String targetUrl = getSafeTargetUrl(request, referer);

                    if (targetUrl != null) {
                        response.sendRedirect(targetUrl);
                        return;
                    }

                    response.sendRedirect("/");
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        return http.build();
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return requestedWith != null && "XMLHttpRequest".equalsIgnoreCase(requestedWith);
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

            if (uri.getPort() != -1 && uri.getPort() != request.getServerPort()) {
                return null;
            }

            String path = uri.getRawPath();
            if (path == null || path.isBlank()) {
                return null;
            }

            if (!isSafeRedirectPath(path)) {
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

    private boolean isSafeRedirectUrl(HttpServletRequest request, String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(redirectUrl);

            if (uri.getHost() == null) {
                return isSafeRedirectPath(redirectUrl);
            }

            if (!request.getServerName().equalsIgnoreCase(uri.getHost())) {
                return false;
            }

            if (uri.getPort() != -1 && uri.getPort() != request.getServerPort()) {
                return false;
            }

            String path = uri.getRawPath();
            if (path == null || path.isBlank()) {
                return false;
            }

            return isSafeRedirectPath(path);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isSafeRedirectPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        if (!path.startsWith("/")) {
            return false;
        }

        if (path.startsWith("//")) {
            return false;
        }

        if (path.startsWith("/login") || path.startsWith("/logout")) {
            return false;
        }

        if (path.startsWith("/register") || path.startsWith("/find-id") || path.startsWith("/find-password")) {
            return false;
        }

        if (path.startsWith("/.well-known/")) {
            return false;
        }

        if (path.startsWith("/api/")) {
            return false;
        }

        if (path.startsWith("/actuator/")) {
            return false;
        }

        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || path.startsWith("/webjars/")) {
            return false;
        }

        if ("/favicon.ico".equals(path)) {
            return false;
        }

        String lower = path.toLowerCase();
        if (lower.endsWith(".json") || lower.endsWith(".map") || lower.endsWith(".xml") || lower.endsWith(".txt")) {
            return false;
        }

        return true;
    }
}
