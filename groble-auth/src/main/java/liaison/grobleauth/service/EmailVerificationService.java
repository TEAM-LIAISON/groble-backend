package liaison.grobleauth.service;

import java.time.Duration;
import java.util.UUID;

import jakarta.mail.MessagingException;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import liaison.grobleauth.dto.AuthDto;
import liaison.groblecore.exception.EmailAlreadyExistsException;
import liaison.groblecore.exception.InvalidTokenException;
import liaison.groblecore.repository.IntegratedAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 이메일 인증 서비스 회원가입 전 이메일 인증을 처리하는 서비스 Redis를 사용하여 인증 토큰과 인증 상태를 관리 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

  private final EmailService emailService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final IntegratedAccountRepository integratedAccountRepository;

  // Redis 키 접두사
  private static final String EMAIL_TOKEN_PREFIX = "email:token:";
  private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";
  private static final String EMAIL_VERIFY_REQUEST_COUNT_PREFIX = "email:request_count:";

  // 토큰 만료 시간 (24시간)
  private static final Duration TOKEN_TTL = Duration.ofHours(24);
  // 인증 완료 정보 보관 시간 (1시간)
  private static final Duration VERIFIED_TTL = Duration.ofHours(1);
  // 동일 이메일 인증 요청 제한 횟수
  private static final int MAX_VERIFICATION_REQUESTS = 5;
  // 인증 요청 카운트 보관 시간 (1시간)
  private static final Duration REQUEST_COUNT_TTL = Duration.ofHours(1);

  /**
   * 이메일 인증 요청 처리 인증 이메일을 발송하고 토큰을 Redis에 저장
   *
   * @param request 이메일 인증 요청 DTO
   * @return 인증 요청 ID (프론트엔드에서 인증 상태 확인에 사용)
   * @throws MessagingException 이메일 발송 실패 시 발생
   * @throws EmailAlreadyExistsException 이미 가입된 이메일인 경우 발생
   */
  public String sendVerificationEmail(AuthDto.EmailVerificationRequest request)
      throws MessagingException, EmailAlreadyExistsException {

    // 이메일 정보 추출
    String email = request.getEmail();

    // 이미 가입된 이메일인지 확인
    if (integratedAccountRepository.existsIntegratedAccountByIntegratedAccountEmail(email)) {
      log.warn("이미 등록된 이메일로 인증 요청: {}", email);
      throw new EmailAlreadyExistsException(email);
    }

    // 인증 요청 횟수 제한 확인 (DoS 방지)
    Integer requestCount =
        (Integer) redisTemplate.opsForValue().get(EMAIL_VERIFY_REQUEST_COUNT_PREFIX + email);
    if (requestCount != null && requestCount >= MAX_VERIFICATION_REQUESTS) {
      log.warn("이메일 인증 요청 횟수 초과: {}, 횟수: {}", email, requestCount);
      throw new RuntimeException("인증 요청 횟수가 초과되었습니다. 1시간 후 다시 시도해주세요.");
    }

    // 인증 요청 횟수 증가
    if (requestCount == null) {
      redisTemplate
          .opsForValue()
          .set(EMAIL_VERIFY_REQUEST_COUNT_PREFIX + email, 1, REQUEST_COUNT_TTL);
    } else {
      redisTemplate.opsForValue().increment(EMAIL_VERIFY_REQUEST_COUNT_PREFIX + email);
    }

    // 해당 이메일에 대한 일회용 토큰 생성
    String token = UUID.randomUUID().toString();

    // Redis에 토큰 저장
    redisTemplate.opsForValue().set(EMAIL_TOKEN_PREFIX + token, email, TOKEN_TTL);

    // 이메일 발송
    emailService.sendVerificationEmail(email, token);

    log.info("인증 이메일 요청 처리 완료: {}", email);

    // 인증 확인을 위한 해시 생성 (프론트엔드에서 인증 상태 확인에 사용)
    String verificationId = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(EMAIL_VERIFIED_PREFIX + verificationId, email, TOKEN_TTL);

    return verificationId;
  }

  /**
   * 이메일 인증 처리 사용자가 이메일의 인증 링크를 클릭했을 때 호출
   *
   * @param token 인증 토큰
   * @return 인증 성공 여부
   * @throws InvalidTokenException 유효하지 않은 토큰인 경우 발생
   */
  public boolean verifyEmail(String token) {
    // 토큰으로 이메일 조회
    String email = (String) redisTemplate.opsForValue().get(EMAIL_TOKEN_PREFIX + token);

    if (email == null) {
      log.warn("유효하지 않은 인증 토큰 사용: {}", token);
      throw new InvalidTokenException("이메일 인증 토큰이 유효하지 않거나 만료되었습니다.");
    }

    // 인증 상태 설정 (이메일 주소를 키로 사용)
    String verifiedKey = EMAIL_VERIFIED_PREFIX + email;
    redisTemplate.opsForValue().set(verifiedKey, true, VERIFIED_TTL);

    // 인증 요청 횟수 초기화
    redisTemplate.delete(EMAIL_VERIFY_REQUEST_COUNT_PREFIX + email);

    // 토큰 삭제 (재사용 방지)
    redisTemplate.delete(EMAIL_TOKEN_PREFIX + token);

    log.info("이메일 인증 완료: {}", email);
    return true;
  }

  /**
   * 이메일 인증 상태 확인 회원가입 진행 전 인증 상태 확인에 사용
   *
   * @param email 확인할 이메일
   * @return 인증 완료 여부
   */
  public boolean isEmailVerified(String email) {
    Boolean verified = (Boolean) redisTemplate.opsForValue().get(EMAIL_VERIFIED_PREFIX + email);
    boolean result = Boolean.TRUE.equals(verified);

    if (result) {
      log.debug("이메일 인증 상태 확인 완료: {}, 인증됨", email);
    } else {
      log.debug("이메일 인증 상태 확인 완료: {}, 미인증", email);
    }

    return result;
  }

  /**
   * 인증된 이메일 조회 회원가입 프로세스에서 인증 ID로 이메일 확인 시 사용
   *
   * @param verificationId 인증 요청 ID
   * @return 인증된 이메일 (인증되지 않은 경우 null)
   */
  public String getVerifiedEmail(String verificationId) {
    // 인증 ID로 이메일 조회
    String email = (String) redisTemplate.opsForValue().get(EMAIL_VERIFIED_PREFIX + verificationId);
    if (email == null) {
      log.debug("유효하지 않은 인증 ID: {}", verificationId);
      return null;
    }

    // 이메일로 인증 상태 확인
    Boolean verified = (Boolean) redisTemplate.opsForValue().get(EMAIL_VERIFIED_PREFIX + email);

    if (!Boolean.TRUE.equals(verified)) {
      log.debug("미인증 이메일: {}, 인증 ID: {}", email, verificationId);
      return null;
    }

    log.debug("인증된 이메일 조회 완료: {}", email);
    return email;
  }

  /**
   * 인증 토큰 만료 처리 회원가입 완료 또는 토큰 무효화 시 호출
   *
   * @param email 이메일
   */
  public void expireVerification(String email) {
    redisTemplate.delete(EMAIL_VERIFIED_PREFIX + email);
    log.debug("이메일 인증 정보 만료 처리: {}", email);
  }
}
