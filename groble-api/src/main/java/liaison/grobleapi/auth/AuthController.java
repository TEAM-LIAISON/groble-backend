package liaison.grobleapi.auth;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.grobleauth.dto.AuthDto;
import liaison.grobleauth.service.AuthService;
import liaison.grobleauth.service.EmailVerificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final EmailVerificationService emailVerificationService;

  /** 회원가입 API */
  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody AuthDto.SignupRequest request) {
    if (authService.signup(request)) {
      return ResponseEntity.ok().body(Map.of("message", "회원가입이 완료되었습니다."));
    } else {
      return ResponseEntity.badRequest().body(Map.of("message", "이미 등록된 이메일입니다."));
    }
  }

  /** 이메일 인증 요청 API */
  @PostMapping("/email/verification-request")
  public ResponseEntity<?> requestVerification(
      @Valid @RequestBody AuthDto.EmailVerificationRequest request) {
    try {
      emailVerificationService.sendVerificationEmail(request);

      return ResponseEntity.ok()
          .body(Map.of("message", "인증 이메일이 발송되었습니다.", "email", request.getEmail()));
    } catch (Exception e) {
      log.error("이메일 발송 실패", e);
      return ResponseEntity.internalServerError().body(Map.of("message", "이메일 발송 중 오류가 발생했습니다."));
    }
  }

  /** 이메일 인증 상태 확인 API */
  @GetMapping("/email/verification-status")
  public ResponseEntity<?> checkVerificationStatus(@RequestParam("email") String email) {
    boolean verified = emailVerificationService.isEmailVerified(email);
    return ResponseEntity.ok().body(Map.of("verified", verified));
  }

  /** 이메일 인증 처리 API */
  @GetMapping("/verify")
  public String verifyEmail(
      @RequestParam("token") String token, @RequestParam("email") String encodedEmail) {

    try {
      // 이메일 디코딩
      byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedEmail);
      String email = new String(decodedBytes);

      // 인증 처리
      boolean verified = emailVerificationService.verifyEmail(token);

      if (verified) {
        // 인증 성공 페이지로 리다이렉트 (이메일 정보 포함)
        return "redirect:/verification-success.html?email=" + encodedEmail;
      } else {
        return "redirect:/verification-failed.html";
      }
    } catch (Exception e) {
      log.error("이메일 인증 실패", e);
      return "redirect:/verification-failed.html?error=" + e.getMessage();
    }
  }
}
