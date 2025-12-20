// MemberApiController.java
package com.hello.community.member;

import com.hello.community.board.common.ApiResponseDto;
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
    public ResponseEntity<ApiResponseDto<Void>> sendEmailCode(@RequestParam String email) throws Exception {
        memberService.checkEmail(email);
        emailVerificationService.sendVerificationCode(email);
        return ResponseEntity.ok(ApiResponseDto.ok());
    }

    @PostMapping("/api/member/email-verify")
    public ResponseEntity<ApiResponseDto<Void>> verifyEmailCode(
            @RequestParam String email,
            @RequestParam String code) {

        boolean ok = emailVerificationService.verifyCode(email, code);
        if (ok) {
            return ResponseEntity.ok(ApiResponseDto.ok());
        }
        return ResponseEntity.badRequest().body(ApiResponseDto.fail("잘못된 인증코드입니다."));
    }

    @PostMapping("/api/member/find-id")
    public ResponseEntity<ApiResponseDto<UsernameData>> findIdByEmail(@RequestParam String email) {
        try {
            String maskedUsername = memberService.findMaskedUsernameByEmail(email);
            return ResponseEntity.ok(ApiResponseDto.ok(new UsernameData(maskedUsername)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @PostMapping("/api/member/find-id/email-code")
    public ResponseEntity<ApiResponseDto<Void>> sendIdFindCode(@RequestParam String email) {
        try {
            memberService.sendIdFindCode(email);
            return ResponseEntity.ok(ApiResponseDto.ok("인증코드를 전송했습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @PostMapping("/api/member/find-id/confirm")
    public ResponseEntity<ApiResponseDto<UsernameData>> confirmIdFind(@RequestParam String email,
                                                                      @RequestParam String code) {
        try {
            String maskedUsername = memberService.confirmIdFindWithCode(email, code);
            return ResponseEntity.ok(ApiResponseDto.ok(new UsernameData(maskedUsername)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @PostMapping("/api/member/password-reset/email-code")
    public ResponseEntity<ApiResponseDto<Void>> sendPasswordResetCode(@RequestParam String username,
                                                                      @RequestParam String email) {
        try {
            memberService.sendPasswordResetCode(username, email);
            return ResponseEntity.ok(ApiResponseDto.ok("인증코드를 전송했습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @PostMapping("/api/member/password-reset/verify")
    public ResponseEntity<ApiResponseDto<Void>> verifyPasswordResetCode(@RequestParam String username,
                                                                        @RequestParam String email,
                                                                        @RequestParam String code) {
        try {
            memberService.verifyPasswordResetCode(username, email, code);
            return ResponseEntity.ok(ApiResponseDto.ok());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @PostMapping("/api/member/password-reset/confirm")
    public ResponseEntity<ApiResponseDto<Void>> resetPasswordWithCode(@RequestParam String username,
                                                                      @RequestParam String email,
                                                                      @RequestParam String code,
                                                                      @RequestParam String newPassword,
                                                                      @RequestParam String newPasswordConfirm
    ) {
        try {
            memberService.resetPasswordWithEmailCode(
                    username, email, code, newPassword, newPasswordConfirm
            );
            return ResponseEntity.ok(ApiResponseDto.ok("비밀번호가 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @PostMapping("/api/member/profile")
    public ResponseEntity<ApiResponseDto<ProfileData>> updateProfile(@RequestParam String newDisplayName,
                                                                     Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
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
                    ApiResponseDto.ok("닉네임이 변경되었습니다.", new ProfileData(updatedMember.getDisplayName()))
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @PostMapping("/api/member/password")
    public ResponseEntity<ApiResponseDto<Void>> updatePassword(@RequestParam String currentPassword,
                                                               @RequestParam String newPassword,
                                                               @RequestParam String newPasswordConfirm,
                                                               Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
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

            return ResponseEntity.ok(ApiResponseDto.ok("비밀번호가 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @PostMapping("/api/member/withdraw/email-code")
    public ResponseEntity<ApiResponseDto<Void>> sendWithdrawEmailCode(Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
        }

        try {
            memberService.sendWithdrawCode(currentUser.getId());
            return ResponseEntity.ok(ApiResponseDto.ok("회원탈퇴 인증코드를 이메일로 전송했습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @PostMapping("/api/member/withdraw")
    public ResponseEntity<ApiResponseDto<Void>> withdraw(@RequestParam String code,
                                                         Authentication auth,
                                                         HttpServletRequest request) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(ApiResponseDto.fail("로그인이 필요합니다."));
        }

        try {
            memberService.withdrawMemberWithCode(currentUser.getId(), code);

            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }

            return ResponseEntity.ok(ApiResponseDto.ok("회원탈퇴가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
        }
    }

    @GetMapping("/api/member/user/{id}")
    public MemberDto user(@PathVariable Long id) {
        return memberService.getMemberDtoById(id);
    }

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

    public static class UsernameData {

        private String username;

        public UsernameData(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class ProfileData {

        private String newDisplayName;

        public ProfileData(String newDisplayName) {
            this.newDisplayName = newDisplayName;
        }

        public String getNewDisplayName() {
            return newDisplayName;
        }
    }
}
