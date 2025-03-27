package liaison.grobleapi.auth;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.grobleauth.dto.AuthDto;
import liaison.grobleauth.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  /** 회원가입 API */
  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody AuthDto.SignupRequest request) {
    if (authService.signup(request)) {
      return ResponseEntity.ok().body(Map.of("message", "회원가입이 완료되었습니다."));
    } else {
      return ResponseEntity.badRequest().body(Map.of("message", "이미 등록된 이메일입니다."));
    }
  }
}
