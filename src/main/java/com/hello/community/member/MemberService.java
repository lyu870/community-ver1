// MemberService.java
package com.hello.community.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public void saveMember(String username,
                           String password,
                           String passwordConfirm,
                           String displayName,
                           String email,
                           String emailCode) throws Exception {

        // 빠른 실패로 불필요한 DB 호출/예외 줄이기
        if (username == null || password == null || passwordConfirm == null
                || displayName == null || email == null || emailCode == null) {
            throw new Exception("입력값이 비어있습니다.");
        }

        // 아이디: 영문+숫자 6자 이상
        if (!username.matches("^[A-Za-z0-9]{6,}$")) {
            throw new Exception("아이디는 영문+숫자 조합 6자 이상이어야 합니다.");
        }

        // 닉네임: 2~8글자, 한글/영문/숫자만, 자음 단독 불가
        if (!displayName.matches("^(?=.*[가-힣A-Za-z0-9])[가-힣A-Za-z0-9]{2,8}$")) {
            throw new Exception("닉네임은 2~8글자의 한글/영문/숫자만 가능합니다.");
        }

        // 비밀번호: 영문 + 숫자 조합, 8자 이상
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            throw new Exception("비밀번호는 영문 + 숫자 조합 8자 이상이어야 합니다.");
        }

        if (!password.equals(passwordConfirm)) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 형식 검증
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new Exception("유효하지 않은 이메일 형식입니다.");
        }

        // 중복 체크
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new Exception("존재하는아이디");
        }

        if (memberRepository.findByDisplayName(displayName).isPresent()) {
            throw new Exception("존재하는닉네임");
        }

        if (memberRepository.findByEmail(email).isPresent()) {
            throw new Exception("존재하는이메일");
        }

        // 이메일 인증코드 검증
        if (!emailVerificationService.verifyCode(email, emailCode)) {
            throw new Exception("이메일 인증번호가 일치하지 않습니다.");
        }

        // 회원 저장
        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(password));
        member.setDisplayName(displayName);
        member.setEmail(email);
        memberRepository.save(member);

        // 인증코드 삭제
        emailVerificationService.deleteCode(email);
    }

    public void checkEmail(String email) throws Exception {
        if (email == null || email.isBlank()) throw new Exception("이메일을 입력하세요.");
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new Exception("유효하지 않은 이메일 형식입니다.");
        }
        if (memberRepository.findByEmail(email).isPresent())
            throw new Exception("이미 사용중인 이메일입니다.");
    }

    public MemberDto getMemberDtoById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다. id=" + id));
        return new MemberDto(member.getUsername(), member.getDisplayName(), member.getId());
    }

    // 닉네임 변경
    @Transactional
    public void changeDisplayName(Long memberId, String newDisplayName) throws Exception {
        if (newDisplayName == null || newDisplayName.isBlank()) {
            throw new Exception("닉네임을 입력하세요.");
        }

        if (!newDisplayName.matches("^(?=.*[가-힣A-Za-z0-9])[가-힣A-Za-z0-9]{2,8}$")) {
            throw new Exception("닉네임은 2~8글자의 한글/영문/숫자만 가능합니다.");
        }

        if (memberRepository.findByDisplayName(newDisplayName).isPresent()) {
            throw new Exception("존재하는닉네임");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception("회원 정보를 찾을 수 없습니다."));

        member.setDisplayName(newDisplayName);
    }

    // 비번 변경
    @Transactional
    public void changePassword(Long memberId,
                               String currentPassword,
                               String newPassword,
                               String newPasswordConfirm) throws Exception {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new Exception("회원 정보를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new Exception("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            throw new Exception("비밀번호는 영문 + 숫자 조합 8자 이상이어야 합니다.");
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        // 현재 비밀번호와 동일한지 체크
        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new Exception("현재 사용 중인 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
    }

    // 아이디 찾기 (마스킹된 username 반환)
    @Transactional(readOnly = true)
    public String findMaskedUsernameByEmail(String email) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("이메일을 입력하세요.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("해당 이메일로 가입된 계정을 찾을 수 없습니다."));

        return FindIdUsername(member.getUsername());
    }

    // 아이디 찾기: 인증코드 이메일 전송
    @Transactional(readOnly = true)
    public void sendIdFindCode(String email) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("이메일을 입력하세요.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("해당 이메일로 가입된 계정을 찾을 수 없습니다."));

        emailVerificationService.sendIdFindCode(member.getEmail());
    }

    // 아이디 찾기: 코드 검증 후 아이디 반환
    @Transactional(readOnly = true)
    public String confirmIdFindWithCode(String email, String code) throws Exception {
        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            throw new Exception("이메일과 인증코드를 모두 입력하세요.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("해당 이메일로 가입된 계정을 찾을 수 없습니다."));

        boolean ok = emailVerificationService.verifyIdFindCode(email, code);
        if (!ok) {
            throw new Exception("인증코드가 올바르지 않습니다.");
        }

        emailVerificationService.deleteIdFindCode(email);

        return FindIdUsername(member.getUsername());
    }

    // 비밀번호 재설정: 인증코드 이메일 전송
    @Transactional(readOnly = true)
    public void sendPasswordResetCode(String username, String email) throws Exception {
        if (username == null || username.isBlank() || email == null || email.isBlank()) {
            throw new Exception("아이디와 이메일을 모두 입력하세요.");
        }

        Member member = memberRepository.findByUsernameAndEmail(username, email)
                .orElseThrow(() -> new Exception("아이디 또는 이메일이 올바르지 않습니다."));

        emailVerificationService.sendPasswordResetCode(member.getEmail());
    }

    // 비밀번호 재설정: 코드 검증
    @Transactional(readOnly = true)
    public void verifyPasswordResetCode(String username, String email, String code) throws Exception {
        if (username == null || username.isBlank() || email == null || email.isBlank()
                || code == null || code.isBlank()) {
            throw new Exception("아이디, 이메일, 인증코드를 모두 입력하세요.");
        }

        memberRepository.findByUsernameAndEmail(username, email)
                .orElseThrow(() -> new Exception("아이디 또는 이메일이 올바르지 않습니다."));

        boolean ok = emailVerificationService.verifyPasswordResetCode(email, code);
        if (!ok) {
            throw new Exception("인증코드가 올바르지 않습니다.");
        }
    }

    // 비밀번호 재설정: 최종 비밀번호 변경
    @Transactional
    public void resetPasswordWithEmailCode(String username,
                                           String email,
                                           String code,
                                           String newPassword,
                                           String newPasswordConfirm)
            throws Exception {

        if (username == null || username.isBlank() || email == null || email.isBlank()
                || code == null || code.isBlank()) {
            throw new Exception("아이디, 이메일, 인증코드를 모두 입력하세요.");
        }

        Member member = memberRepository.findByUsernameAndEmail(username, email)
                .orElseThrow(() -> new Exception("아이디 또는 이메일이 올바르지 않습니다."));

        boolean ok = emailVerificationService.verifyPasswordResetCode(email, code);
        if (!ok) {
            throw new Exception("인증코드가 올바르지 않습니다.");
        }

        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            throw new Exception("비밀번호는 영문 + 숫자 조합 8자 이상이어야 합니다.");
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new Exception("이전에 사용한 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
        }

        member.setPassword(passwordEncoder.encode(newPassword));

        emailVerificationService.deletePasswordResetCode(email);
    }

    // 아이디찾기값 반환 (나중에 마스킹된 ID를 반환하게 할거면 이 함수 수정)
    private String FindIdUsername(String username) {
        return username;
    }
}
