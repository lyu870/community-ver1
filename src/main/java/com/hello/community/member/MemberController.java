// MemberController.java
package com.hello.community.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    // 변경: API 분리로 인해 여기서는 페이지 렌더링/폼 submit만 담당
    private final MemberService memberService;

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

    // 회원 탈퇴 페이지
    @GetMapping("/my-page/withdraw")
    public String withdrawPage(Authentication auth, Model model) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUser user)) {
            return "redirect:/login";
        }

        MemberDto dto = memberService.getMemberDtoById(user.getId());
        model.addAttribute("member", dto);
        return "member/withdraw";
    }
}
