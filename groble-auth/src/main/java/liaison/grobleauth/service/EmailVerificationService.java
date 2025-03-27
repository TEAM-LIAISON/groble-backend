package liaison.grobleauth.service;

import java.time.Duration;
import java.util.UUID;

import jakarta.mail.MessagingException;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import liaison.grobleauth.dto.AuthDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

  private final EmailService emailService;
  private final RedisTemplate<String, Object> redisTemplate;

  // Redis 키 접두사
  private static final String EMAIL_TOKEN_PREFIX = "email:token:";
  private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";

  // 토큰 만료 시간 (24시간)
  private static final Duration TOKEN_TTL = Duration.ofHours(24);
  // 인증 완료 정보 보관 시간 (1시간)
  private static final Duration VERIFIED_TTL = Duration.ofHours(1);

  /** 이메일 인증 요청 처리 */
  public String sendVerificationEmail(AuthDto.EmailVerificationRequest request)
      throws MessagingException {
    // 해당 이메일에 대한 일회용 토큰 생성
    String token = UUID.randomUUID().toString();
    String email = request.getEmail();

    // Redis에 토큰 저장
    redisTemplate.opsForValue().set(EMAIL_TOKEN_PREFIX + token, email, TOKEN_TTL);

    // 이메일 발송
    emailService.sendVerificationEmail(email, token);

    log.info("인증 이메일 요청 처리 완료: {}", email);

    // 인증 확인을 위한 해시 생성
    String verificationId = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(EMAIL_VERIFIED_PREFIX + verificationId, email, TOKEN_TTL);

    return verificationId;
  }

  /** 이메일 인증 처리 */
  public boolean verifyEmail(String token) {
    // 토큰으로 이메일 조회
    String email = (String) redisTemplate.opsForValue().get(EMAIL_TOKEN_PREFIX + token);

    if (email == null) {
      throw new RuntimeException("유효하지 않은 인증 토큰입니다.");
    }

    // 인증 상태 설정
    String verifiedKey = EMAIL_VERIFIED_PREFIX + email;
    redisTemplate.opsForValue().set(verifiedKey, true, VERIFIED_TTL);

    log.info("이메일 인증 완료: {}", email);
    return true;
  }

  /** 이메일 인증 상태 확인 */
  public boolean isEmailVerified(String email) {
    Boolean verified = (Boolean) redisTemplate.opsForValue().get(EMAIL_VERIFIED_PREFIX + email);
    return Boolean.TRUE.equals(verified);
  }

  /** 인증된 이메일 조회 (회원가입 시 사용) */
  public String getVerifiedEmail(String verificationId) {
    String email = (String) redisTemplate.opsForValue().get(EMAIL_VERIFIED_PREFIX + verificationId);
    if (email == null) {
      return null;
    }

    Boolean verified = (Boolean) redisTemplate.opsForValue().get(EMAIL_VERIFIED_PREFIX + email);
    return Boolean.TRUE.equals(verified) ? email : null;
  }
}
