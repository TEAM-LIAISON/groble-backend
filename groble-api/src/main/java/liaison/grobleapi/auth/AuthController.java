package liaison.grobleapi.auth;

import java.util.Base64;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

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

  /** 로그인 API */
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody AuthDto.LoginRequest request) {
    String token = authService.login(request);
    if (token != null) {
      return ResponseEntity.ok().body(Map.of("token", token));
    } else {
      return ResponseEntity.badRequest().body(Map.of("message", "이메일 또는 비밀번호가 일치하지 않습니다."));
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

  @GetMapping("/verify")
  public RedirectView verifyEmail(
      @RequestParam("token") String token, @RequestParam("email") String encodedEmail) {

    try {
      boolean verified = emailVerificationService.verifyEmail(token);

      if (verified) {
        return new RedirectView("/verification-success.html?email=" + encodedEmail);
      } else {
        return new RedirectView("/verification-failed.html");
      }
    } catch (Exception e) {
      return new RedirectView("/verification-failed.html?error=" + e.getMessage());
    }
  }

  // 테스트용 컨트롤러 (개발 완료 후 제거)
  @GetMapping("/test-verification-success")
  public String testVerificationSuccess(@RequestParam("email") String email) {
    // 이메일을 Base64로 인코딩
    String encodedEmail = Base64.getEncoder().encodeToString(email.getBytes());
    return "redirect:/verification-success.html?email=" + encodedEmail;
  }
}
