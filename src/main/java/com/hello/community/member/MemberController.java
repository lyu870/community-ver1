// MemberController.java
package com.hello.community.member;

import com.hello.community.member.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/register")
    public String register() {
        return "member/register";
    }

    @PostMapping("/member")
    public String addMember(String username,
                            String password,
                            String passwordConfirm,
                            String displayName,
                            String email,
                            String emailCode) throws Exception {
        // 유저가 입력한 아이디비번이름 DB에 저장하기
        memberService.saveMember(username, password, passwordConfirm, displayName, email, emailCode);
        return "member/login";
    }

    @PostMapping("/member/email-code")
    @ResponseBody
    public String sendEmailCode(@RequestParam String email) throws Exception {
        memberService.checkEmail(email);
        emailVerificationService.sendVerificationCode(email);
        return "OK";
    }

    @GetMapping("/login")
    public String login() {
        return "member/login";
    }

    // ID 찾기 페이지
    @GetMapping("/find-id")
    public String findIdPage() {
        return "member/find-id";
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/find-password")
    public String findPasswordPage() {
        return "member/find-pw";
    }

    // ID 찾기 처리 (구 방식: 이메일만으로 바로 찾기 - 현재 UI에서는 사용하지 않을 수 있음)
    @PostMapping("/member/find-id")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> findIdByEmail(@RequestParam String email) {
        try {
            String maskedUsername = memberService.findMaskedUsernameByEmail(email);
            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "username", maskedUsername
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    // ID 찾기: 인증코드 이메일 전송
    @PostMapping("/member/find-id/email-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendIdFindCode(@RequestParam String email) {
        try {
            memberService.sendIdFindCode(email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "인증코드를 전송했습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // ID 찾기: 인증코드 검증 후 아이디 반환
    @PostMapping("/member/find-id/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmIdFind(@RequestParam String email,
                                                             @RequestParam String code) {
        try {
            String maskedUsername = memberService.confirmIdFindWithCode(email, code);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", maskedUsername
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // 비밀번호 재설정: 인증코드 이메일 전송 (AJAX)
    @PostMapping("/member/password-reset/email-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendPasswordResetCode(@RequestParam String username,
                                                                     @RequestParam String email) {
        try {
            memberService.sendPasswordResetCode(username, email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "인증코드를 전송했습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // 비밀번호 재설정: 코드 검증 (AJAX)
    @PostMapping("/member/password-reset/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyPasswordResetCode(@RequestParam String username,
                                                                       @RequestParam String email,
                                                                       @RequestParam String code) {
        try {
            memberService.verifyPasswordResetCode(username, email, code);
            return ResponseEntity.ok(Map.of(
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // 비밀번호 재설정: 최종 비밀번호 변경 (AJAX)
    @PostMapping("/member/password-reset/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetPasswordWithCode(@RequestParam String username,
                                                                     @RequestParam String email,
                                                                     @RequestParam String code,
                                                                     @RequestParam String newPassword,
                                                                     @RequestParam String newPasswordConfirm
    ) {
        try {
            memberService.resetPasswordWithEmailCode(
                    username, email, code, newPassword, newPasswordConfirm
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "비밀번호가 변경되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // 마이페이지
    @GetMapping("/my-page")
    public String myPage(Authentication auth, Model model) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUser user)) {
            return "redirect:/login";
        }

        MemberDto dto = memberService.getMemberDtoById(user.getId());
        model.addAttribute("member", dto);
        return "member/mypage";
    }

    // 닉네임 변경 : AJAX JSON 응답
    @PostMapping("/my-page/profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestParam String newDisplayName,
                                                             Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "로그인이 필요합니다."
            ));
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

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "닉네임이 변경되었습니다.",
                    "newDisplayName", updatedMember.getDisplayName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // 비밀번호 변경 : AJAX JSON 응답
    @PostMapping("/my-page/password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestParam String currentPassword,
                                                              @RequestParam String newPassword,
                                                              @RequestParam String newPasswordConfirm,
                                                              Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "로그인이 필요합니다."
            ));
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

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "비밀번호가 변경되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // 회원 탈퇴 : AJAX JSON 응답
    @PostMapping("/my-page/withdraw")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> withdraw(Authentication auth) {

        if (auth == null || !(auth.getPrincipal() instanceof CustomUser currentUser)) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "로그인이 필요합니다."
            ));
        }

        try {
            memberService.withdrawMember(currentUser.getId());

            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원탈퇴가 완료되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{id}")
    @ResponseBody
    public MemberDto user(@PathVariable Long id) {
        return memberService.getMemberDtoById(id);
    }

    // 회원가입 시 데이터 중복확인
    @PostMapping("/member/check-username")
    @ResponseBody
    public ResponseEntity<String> checkUsername(@RequestParam String username) {
        if (username == null || username.isBlank()) return ResponseEntity.ok("EMPTY");
        boolean exists = memberRepository.findByUsername(username).isPresent();
        return ResponseEntity.ok(exists ? "EXISTS" : "OK");
    }

    @PostMapping("/member/check-displayName")
    @ResponseBody
    public ResponseEntity<String> checkDisplayName(@RequestParam String displayName) {
        if (displayName == null || displayName.isBlank()) return ResponseEntity.ok("EMPTY");
        boolean exists = memberRepository.findByDisplayName(displayName).isPresent();
        return ResponseEntity.ok(exists ? "EXISTS" : "OK");
    }

    @PostMapping("/member/check-email")
    @ResponseBody
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        if (email == null || email.isBlank()) return ResponseEntity.ok("EMPTY");
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.ok("INVALID");
        }
        boolean exists = memberRepository.findByEmail(email).isPresent();
        return ResponseEntity.ok(exists ? "EXISTS" : "OK");
    }

    @PostMapping("/member/email-verify")
    @ResponseBody
    public ResponseEntity<String> verifyEmailCode(
            @RequestParam String email,
            @RequestParam String code) {

        boolean ok = emailVerificationService.verifyCode(email, code);
        return ResponseEntity.ok(ok ? "OK" : "FAIL");
    }
}
