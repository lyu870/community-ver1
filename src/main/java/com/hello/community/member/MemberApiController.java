// MemberApiController.java
package com.hello.community.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/api/member/email-code")
    public String sendEmailCode(@RequestParam String email) throws Exception {
        memberService.checkEmail(email);
        emailVerificationService.sendVerificationCode(email);
        return "OK";
    }

    @PostMapping("/api/member/email-verify")
    public ResponseEntity<String> verifyEmailCode(
            @RequestParam String email,
            @RequestParam String code) {

        boolean ok = emailVerificationService.verifyCode(email, code);
        return ResponseEntity.ok(ok ? "OK" : "FAIL");
    }

    // ID 찾기 처리 (구 방식: 이메일만으로 바로 찾기 - 현재 UI에서는 사용하지 않을 수 있음)
    @PostMapping("/api/member/find-id")
    public ResponseEntity<FindIdByEmailResponseDto> findIdByEmail(@RequestParam String email) {
        try {
            String maskedUsername = memberService.findMaskedUsernameByEmail(email);
            return ResponseEntity.ok(FindIdByEmailResponseDto.ok(maskedUsername));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(FindIdByEmailResponseDto.fail(e.getMessage()));
        }
    }

    // ID 찾기: 인증코드 이메일 전송
    @PostMapping("/api/member/find-id/email-code")
    public ResponseEntity<ApiMessageResponseDto> sendIdFindCode(@RequestParam String email) {
        try {
            memberService.sendIdFindCode(email);
            return ResponseEntity.ok(ApiMessageResponseDto.ok("인증코드를 전송했습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiMessageResponseDto.fail(e.getMessage()));
        }
    }

    // ID 찾기: 인증코드 검증 후 아이디 반환
    @PostMapping("/api/member/find-id/confirm")
    public ResponseEntity<ApiUsernameResponseDto> confirmIdFind(@RequestParam String email,
                                                                @RequestParam String code) {
        try {
            String maskedUsername = memberService.confirmIdFindWithCode(email, code);
            return ResponseEntity.ok(ApiUsernameResponseDto.ok(maskedUsername));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiUsernameResponseDto.fail(e.getMessage()));
        }
    }

    // 비밀번호 재설정: 인증코드 이메일 전송 (AJAX)
    @PostMapping("/api/member/password-reset/email-code")
    public ResponseEntity<ApiMessageResponseDto> sendPasswordResetCode(@RequestParam String username,
                                                                       @RequestParam String email) {
        try {
            memberService.sendPasswordResetCode(username, email);
            return ResponseEntity.ok(ApiMessageResponseDto.ok("인증코드를 전송했습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiMessageResponseDto.fail(e.getMessage()));
        }
    }

    // 비밀번호 재설정: 코드 검증 (AJAX)
    @PostMapping("/api/member/password-reset/verify")
    public ResponseEntity<ApiMessageResponseDto> verifyPasswordResetCode(@RequestParam String username,
                                                                         @RequestParam String email,
                                                                         @RequestParam String code) {
        try {
            memberService.verifyPasswordResetCode(username, email, code);
            return ResponseEntity.ok(ApiMessageResponseDto.ok());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiMessageResponseDto.fail(e.getMessage()));
        }
    }

    // 비밀번호 재설정: 최종 비밀번호 변경 (AJAX)
    @PostMapping("/api/member/password-reset/confirm")
    public ResponseEntity<ApiMessageResponseDto> resetPasswordWithCode(@RequestParam String username,
                                                                       @RequestParam String email,
                                                                       @RequestParam String code,
                                                                       @RequestParam String newPassword,
                                                                       @RequestParam String newPasswordConfirm
    ) {
        try {
            memberService.resetPasswordWithEmailCode(
                    username, email, code, newPassword, newPasswordConfirm
            );
            return ResponseEntity.ok(ApiMessageResponseDto.ok("비밀번호가 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiMessageResponseDto.fail(e.getMessage()));
        }
    }

    // 닉네임 변경 : AJAX JSON 응답
    @PostMapping("/api/member/profile")
    public ResponseEntity<ApiProfileResponseDto> updateProfile(@RequestParam String newDisplayName,
                                                               Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(ApiProfileResponseDto.fail("로그인이 필요합니다."));
        }

        try {
            memberService.changeDisplayName(currentUser.getId(), newDisplayName);

            var updatedMember = memberRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

            CustomUser newPrincipal = new CustomUser(updatedMember, currentUser.getAuthorities());

            UsernamePasswordAuthenticationToken newAuth =
                    new UsernamePasswordAuthenticationToken(
                            newPrincipal,
                            newPrincipal.getPassword(),
                            newPrincipal.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(newAuth);

            return ResponseEntity.ok(
                    ApiProfileResponseDto.ok("닉네임이 변경되었습니다.", updatedMember.getDisplayName())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiProfileResponseDto.fail(e.getMessage()));
        }
    }

    // 비밀번호 변경 : AJAX JSON 응답
    @PostMapping("/api/member/password")
    public ResponseEntity<ApiMessageResponseDto> updatePassword(@RequestParam String currentPassword,
                                                                @RequestParam String newPassword,
                                                                @RequestParam String newPasswordConfirm,
                                                                Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(ApiMessageResponseDto.fail("로그인이 필요합니다."));
        }

        try {
            memberService.changePassword(
                    currentUser.getId(),
                    currentPassword,
                    newPassword,
                    newPasswordConfirm
            );

            var updatedMember = memberRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

            CustomUser newPrincipal = new CustomUser(updatedMember, currentUser.getAuthorities());

            UsernamePasswordAuthenticationToken newAuth =
                    new UsernamePasswordAuthenticationToken(
                            newPrincipal,
                            newPrincipal.getPassword(),
                            newPrincipal.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(newAuth);

            return ResponseEntity.ok(ApiMessageResponseDto.ok("비밀번호가 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiMessageResponseDto.fail(e.getMessage()));
        }
    }

    // 회원 탈퇴용 인증코드 이메일 전송 : AJAX JSON 응답
    @PostMapping("/api/member/withdraw/email-code")
    public ResponseEntity<ApiMessageResponseDto> sendWithdrawEmailCode(Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(ApiMessageResponseDto.fail("로그인이 필요합니다."));
        }

        try {
            memberService.sendWithdrawCode(currentUser.getId());
            return ResponseEntity.ok(ApiMessageResponseDto.ok("회원탈퇴 인증코드를 이메일로 전송했습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiMessageResponseDto.fail(e.getMessage()));
        }
    }

    // 회원 탈퇴 : AJAX JSON 응답
    @PostMapping("/api/member/withdraw")
    public ResponseEntity<ApiMessageResponseDto> withdraw(@RequestParam String code,
                                                          Authentication auth,
                                                          HttpServletRequest request) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(ApiMessageResponseDto.fail("로그인이 필요합니다."));
        }

        try {
            memberService.withdrawMemberWithCode(currentUser.getId(), code);

            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }

            return ResponseEntity.ok(ApiMessageResponseDto.ok("회원탈퇴가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiMessageResponseDto.fail(e.getMessage()));
        }
    }

    @GetMapping("/api/member/user/{id}")
    public MemberDto user(@PathVariable Long id) {
        return memberService.getMemberDtoById(id);
    }

    // 회원가입 시 데이터 중복확인
    @PostMapping("/api/member/check-username")
    public ResponseEntity<String> checkUsername(@RequestParam String username) {
        if (username == null || username.isBlank()) return ResponseEntity.ok("EMPTY");
        boolean exists = memberRepository.findByUsername(username).isPresent();
        return ResponseEntity.ok(exists ? "EXISTS" : "OK");
    }

    @PostMapping("/api/member/check-displayName")
    public ResponseEntity<String> checkDisplayName(@RequestParam String displayName) {
        if (displayName == null || displayName.isBlank()) return ResponseEntity.ok("EMPTY");
        boolean exists = memberRepository.findByDisplayName(displayName).isPresent();
        return ResponseEntity.ok(exists ? "EXISTS" : "OK");
    }

    @PostMapping("/api/member/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        if (email == null || email.isBlank()) return ResponseEntity.ok("EMPTY");
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.ok("INVALID");
        }
        boolean exists = memberRepository.findByEmail(email).isPresent();
        return ResponseEntity.ok(exists ? "EXISTS" : "OK");
    }
}
