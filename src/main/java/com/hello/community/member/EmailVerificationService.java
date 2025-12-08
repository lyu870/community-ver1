// EmailVerificationService.java
package com.hello.community.member;

import com.hello.community.member.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    private static final String PREFIX = "email:verify:";
    private static final String ID_PREFIX = "email:findid:";
    private static final String PW_PREFIX = "email:pwreset:";
    private static final String WITHDRAW_PREFIX = "email:withdraw:";

    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(PREFIX + email, code, Duration.ofMinutes(5));

        String subject = "회원가입 인증번호";

        String html = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>이메일 인증번호</title>
                </head>
                <body style="margin:0; padding:0; background:#f5f6f7; font-family:Arial, sans-serif;">

                <div style="max-width:480px; margin:40px auto; background:#ffffff; border-radius:10px; padding:30px; box-shadow:0 4px 12px rgba(0,0,0,0.08);">

                    <h2 style="text-align:center; color:#333; margin-bottom:10px;">
                        커뮤니티 회원가입 인증번호
                    </h2>

                    <p style="text-align:center; color:#555; font-size:14px; margin-bottom:30px;">
                        아래 인증번호를 회원가입 창에 입력해주세요.
                    </p>

                    <div style="text-align:center; margin:30px 0;">
                        <span style="display:inline-block; font-size:42px; font-weight:bold; letter-spacing:6px; color:#2c7efb;">
                            %s
                        </span>
                    </div>

                    <p style="text-align:center; color:#888; font-size:13px;">
                        인증코드 유효시간은 <b>5분</b>입니다.
                    </p>

                </div>

                <p style="text-align:center; color:#aaa; font-size:12px; margin-top:10px;">
                    © Community Service
                </p>

                </body>
                </html>
                """.formatted(code);

        emailService.sendHtmlMail(email, subject, html);
    }

    public boolean verifyCode(String email, String code) {
        String saved = redisTemplate.opsForValue().get(PREFIX + email);
        return saved != null && saved.equals(code);
    }

    public void deleteCode(String email) {
        redisTemplate.delete(PREFIX + email);
    }

    // 아이디 찾기용 인증코드 전송
    public void sendIdFindCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(ID_PREFIX + email, code, Duration.ofMinutes(5));

        String subject = "아이디 찾기 인증번호";

        String html = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>아이디 찾기 인증번호</title>
                </head>
                <body style="margin:0; padding:0; background:#f5f6f7; font-family:Arial, sans-serif;">

                <div style="max-width:480px; margin:40px auto; background:#ffffff; border-radius:10px; padding:30px; box-shadow:0 4px 12px rgba(0,0,0,0.08);">

                    <h2 style="text-align:center; color:#333; margin-bottom:10px;">
                        아이디 찾기 인증번호
                    </h2>

                    <p style="text-align:center; color:#555; font-size:14px; margin-bottom:30px;">
                        아래 인증번호를 아이디 찾기 화면에 입력해주세요.
                    </p>

                    <div style="text-align:center; margin:30px 0;">
                        <span style="display:inline-block; font-size:42px; font-weight:bold; letter-spacing:6px; color:#2c7efb;">
                            %s
                        </span>
                    </div>

                    <p style="text-align:center; color:#888; font-size:13px;">
                        인증코드 유효시간은 <b>5분</b>입니다.
                    </p>

                </div>

                <p style="text-align:center; color:#aaa; font-size:12px; margin-top:10px;">
                    © Community Service
                </p>

                </body>
                </html>
                """.formatted(code);

        emailService.sendHtmlMail(email, subject, html);
    }

    // 아이디 찾기용 코드 검증
    public boolean verifyIdFindCode(String email, String code) {
        String saved = redisTemplate.opsForValue().get(ID_PREFIX + email);
        return saved != null && saved.equals(code);
    }

    // 아이디 찾기용 코드 삭제
    public void deleteIdFindCode(String email) {
        redisTemplate.delete(ID_PREFIX + email);
    }

    // 비밀번호 재설정용 인증코드 전송
    public void sendPasswordResetCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(PW_PREFIX + email, code, Duration.ofMinutes(5));

        String subject = "비밀번호 재설정 인증번호";

        String html = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>비밀번호 재설정 인증번호</title>
                </head>
                <body style="margin:0; padding:0; background:#f5f6f7; font-family:Arial, sans-serif;">

                <div style="max-width:480px; margin:40px auto; background:#ffffff; border-radius:10px; padding:30px; box-shadow:0 4px 12px rgba(0,0,0,0.08);">

                    <h2 style="text-align:center; color:#333; margin-bottom:10px;">
                        비밀번호 재설정 인증번호
                    </h2>

                    <p style="text-align:center; color:#555; font-size:14px; margin-bottom:30px;">
                        아래 인증번호를 비밀번호 찾기 화면에 입력해주세요.
                    </p>

                    <div style="text-align:center; margin:30px 0;">
                        <span style="display:inline-block; font-size:42px; font-weight:bold; letter-spacing:6px; color:#2c7efb;">
                            %s
                        </span>
                    </div>

                    <p style="text-align:center; color:#888; font-size:13px;">
                        인증코드 유효시간은 <b>5분</b>입니다.
                    </p>

                </div>

                <p style="text-align:center; color:#aaa; font-size:12px; margin-top:10px;">
                    © Community Service
                </p>

                </body>
                </html>
                """.formatted(code);

        emailService.sendHtmlMail(email, subject, html);
    }

    // 비밀번호 재설정용 코드 검증
    public boolean verifyPasswordResetCode(String email, String code) {
        String saved = redisTemplate.opsForValue().get(PW_PREFIX + email);
        return saved != null && saved.equals(code);
    }

    // 비밀번호 재설정용 코드 삭제
    public void deletePasswordResetCode(String email) {
        redisTemplate.delete(PW_PREFIX + email);
    }

    // 회원탈퇴용 인증코드 전송
    public void sendWithdrawCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(WITHDRAW_PREFIX + email, code, Duration.ofMinutes(5));

        String subject = "회원탈퇴 인증번호";

        String html = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>회원탈퇴 인증번호</title>
                </head>
                <body style="margin:0; padding:0; background:#f5f6f7; font-family:Arial, sans-serif;">

                <div style="max-width:480px; margin:40px auto; background:#ffffff; border-radius:10px; padding:30px; box-shadow:0 4px 12px rgba(0,0,0,0.08);">

                    <h2 style="text-align:center; color:#333; margin-bottom:10px;">
                        회원탈퇴 인증번호
                    </h2>

                    <p style="text-align:center; color:#555; font-size:14px; margin-bottom:30px;">
                        아래 인증번호를 마이페이지의 회원탈퇴 영역에 입력해주세요.
                    </p>

                    <div style="text-align:center; margin:30px 0;">
                        <span style="display:inline-block; font-size:42px; font-weight:bold; letter-spacing:6px; color:#2c7efb;">
                            %s
                        </span>
                    </div>

                    <p style="text-align:center; color:#888; font-size:13px;">
                        인증코드 유효시간은 <b>5분</b>입니다.
                    </p>

                </div>

                <p style="text-align:center; color:#aaa; font-size:12px; margin-top:10px;">
                    © Community Service
                </p>

                </body>
                </html>
                """.formatted(code);

        emailService.sendHtmlMail(email, subject, html);
    }

    // 회원탈퇴용 코드 검증
    public boolean verifyWithdrawCode(String email, String code) {
        String saved = redisTemplate.opsForValue().get(WITHDRAW_PREFIX + email);
        return saved != null && saved.equals(code);
    }

    // 회원탈퇴용 코드 삭제
    public void deleteWithdrawCode(String email) {
        redisTemplate.delete(WITHDRAW_PREFIX + email);
    }
}
