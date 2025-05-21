package liaison.groble.application.user.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.application.user.service.PortOneIdentityVerificationService;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.user.entity.IdentityVerificationHistory;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.IdentityVerificationStatus;
import liaison.groble.domain.user.repository.IdentityVerificationHistoryRepository;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.vo.IdentityVerification;
import liaison.groble.external.config.PortOneProperties;
import liaison.groble.external.payment.PortOneApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneIdentityVerificationServiceImpl implements PortOneIdentityVerificationService {

  private final PortOneApiClient portOneClient;
  private final PortOneProperties portOneProperties;
  private final UserRepository userRepository;
  private final IdentityVerificationHistoryRepository verificationHistoryRepository;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public String createVerificationRequest(
      User user, IdentityVerification.VerificationMethod type, String returnUrl) {
    log.info("Creating identity verification request for user: {}, method: {}", user.getId(), type);

    // V2 API용 요청 데이터 준비
    Map<String, Object> requestData = new HashMap<>();
    String requestId =
        "IDENTITY_" + user.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);

    // 본인인증 요청 데이터 구성
    Map<String, Object> identityVerificationRequest = new HashMap<>();
    identityVerificationRequest.put("orderId", requestId);
    identityVerificationRequest.put("name", user.getNickname());
    identityVerificationRequest.put("phoneNumber", user.getPhoneNumber());

    // 인증 방법에 따른 PG 및 방법 설정
    Map<String, Object> identityMethodOptions = new HashMap<>();
    switch (type) {
      case PHONE:
        identityVerificationRequest.put("pgProvider", "danal");
        identityMethodOptions.put("method", "휴대폰인증");
        break;
      case CARD:
        identityVerificationRequest.put("pgProvider", "inicis");
        identityMethodOptions.put("method", "카드인증");
        break;
      case KAKAO:
        identityVerificationRequest.put("pgProvider", "kakao");
        identityMethodOptions.put("method", "카카오인증");
        break;
      default:
        throw new IllegalArgumentException("지원하지 않는 인증 방법입니다: " + type);
    }

    identityVerificationRequest.put("identityMethodOptions", identityMethodOptions);

    // 콜백 URL 설정
    Map<String, String> callbackUrls = new HashMap<>();
    callbackUrls.put(
        "successUrl",
        returnUrl != null ? returnUrl : portOneProperties.getIdentityRedirectUrl() + "/success");
    callbackUrls.put(
        "failUrl",
        returnUrl != null
            ? returnUrl.replace("/success", "/fail")
            : portOneProperties.getIdentityRedirectUrl() + "/fail");
    identityVerificationRequest.put("callbackUrls", callbackUrls);

    requestData.put("identityVerificationRequest", identityVerificationRequest);

    // 요청 이력 저장
    IdentityVerificationHistory history =
        IdentityVerificationHistory.builder()
            .user(user)
            .requestIp("127.0.0.1") // 실제 구현에서는 클라이언트 IP 사용
            .userAgent("User-Agent") // 실제 구현에서는 클라이언트의 User-Agent 사용
            .verificationMethod(type)
            .beforeStatus(IdentityVerificationStatus.NONE)
            .transactionId(requestId)
            .portOneRequestId(requestId)
            .rawRequest(requestData)
            .build();
    verificationHistoryRepository.save(history);

    try {
      // 포트원 V2 API 호출
      Map<String, Object> response =
          portOneClient.callApi("/identity-verifications", HttpMethod.POST, requestData, Map.class);

      // 응답에서 리다이렉트 URL 추출
      Map<String, Object> result = (Map<String, Object>) response.get("identityVerificationResult");
      String redirectUrl = (String) result.get("redirectUrl");

      // 이력 업데이트
      history.markAsUpdate(IdentityVerificationStatus.REQUESTED, response);
      verificationHistoryRepository.save(history);

      log.info(
          "Created identity verification request: {}, redirectUrl: {}", requestId, redirectUrl);
      return redirectUrl;

    } catch (Exception e) {
      log.error("Failed to create identity verification request", e);
      history.markAsFailure(
          IdentityVerificationStatus.FAILED,
          "ERROR",
          e.getMessage(),
          Map.of("error", e.getMessage()));
      verificationHistoryRepository.save(history);
      throw new RuntimeException("본인인증 요청 생성에 실패했습니다", e);
    }
  }

  @Override
  @Transactional
  public IdentityVerification processVerificationResult(String authToken, String state) {
    log.info("Processing identity verification result: token={}, state={}", authToken, state);

    try {
      // V2 API로 인증 결과 조회
      Map<String, Object> requestData = new HashMap<>();
      requestData.put("authToken", authToken);

      Map<String, Object> response =
          portOneClient.callApi(
              "/identity-verifications/result", HttpMethod.POST, requestData, Map.class);

      // 응답에서 필요한 정보 추출
      Map<String, Object> resultData =
          (Map<String, Object>) response.get("identityVerificationResult");
      String merchantUid = (String) resultData.get("orderId");

      // 이력 조회
      IdentityVerificationHistory history =
          verificationHistoryRepository
              .findByTransactionId(merchantUid)
              .orElseThrow(
                  () -> new EntityNotFoundException("해당 요청 ID로 인증 이력을 찾을 수 없습니다: " + merchantUid));

      User user = history.getUser();

      // 개인 정보 추출
      Map<String, Object> personalData = (Map<String, Object>) resultData.get("personalData");
      String ci = (String) personalData.get("ci");
      String di = (String) personalData.get("di");
      String name = (String) personalData.get("name");
      String gender = (String) personalData.get("gender");
      String birthdate = (String) personalData.get("birthDate");
      String phone = (String) personalData.get("phoneNumber");

      // 생년월일 변환 (YYYYMMDD 형식으로 가정)
      LocalDate birthDate = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("yyyyMMdd"));

      // 인증 정보 생성
      IdentityVerification verification =
          IdentityVerification.builder()
              .verificationMethod(history.getVerificationMethod())
              .verifiedName(name)
              .verifiedBirthDate(birthDate)
              .verifiedGender(gender)
              .verifiedPhone(phone)
              .verifiedNationality("KR") // 기본값은 한국
              .ci(ci)
              .di(di)
              .certificationProvider("portone")
              .certificationTxId(authToken)
              .portOneRequestId(merchantUid)
              .portOneTransactionId(authToken)
              .verificationData(response)
              .build();

      // 사용자 정보 업데이트
      user.completeIdentityVerification(verification);
      userRepository.save(user);

      // 이력 업데이트
      history.markAsSuccess(IdentityVerificationStatus.VERIFIED, response);
      verificationHistoryRepository.save(history);

      log.info("Successfully processed identity verification for user: {}", user.getId());
      return verification;

    } catch (Exception e) {
      log.error("Failed to process identity verification result", e);
      throw new RuntimeException("본인인증 결과 처리에 실패했습니다", e);
    }
  }

  @Override
  public boolean validateUserByCiDi(String ci, String di, User user) {
    log.debug("Validating user by CI/DI: user={}", user.getId());

    if (user.getIdentityVerification() == null) {
      log.warn("User has no identity verification: {}", user.getId());
      return false;
    }

    IdentityVerification verification = user.getIdentityVerification();
    boolean valid = ci.equals(verification.getCi()) && di.equals(verification.getDi());

    log.debug("User CI/DI validation result: {}", valid);
    return valid;
  }

  @Override
  public IdentityVerificationStatus getVerificationStatus(String requestId) {
    log.debug("Getting verification status for request: {}", requestId);

    try {
      // V2 API로 인증 상태 조회
      Map<String, Object> requestData = new HashMap<>();
      requestData.put("orderId", requestId);

      Map<String, Object> response =
          portOneClient.callApi(
              "/identity-verifications/status", HttpMethod.POST, requestData, Map.class);

      Map<String, Object> resultData =
          (Map<String, Object>) response.get("identityVerificationStatusResult");
      String status = (String) resultData.get("status");

      // 상태값 변환
      IdentityVerificationStatus result;
      switch (status) {
        case "READY":
          result = IdentityVerificationStatus.REQUESTED;
          break;
        case "IN_PROGRESS":
          result = IdentityVerificationStatus.IN_PROGRESS;
          break;
        case "COMPLETED":
        case "DONE":
          result = IdentityVerificationStatus.VERIFIED;
          break;
        case "FAILED":
          result = IdentityVerificationStatus.FAILED;
          break;
        case "EXPIRED":
          result = IdentityVerificationStatus.EXPIRED;
          break;
        default:
          result = IdentityVerificationStatus.NONE;
      }

      log.debug("Verification status for request {}: {}", requestId, result);
      return result;

    } catch (Exception e) {
      log.warn("Failed to get verification status from API, falling back to DB: {}", requestId, e);

      // API 조회 실패 시 DB에서 상태 조회
      return verificationHistoryRepository
          .findByPortOneRequestId(requestId)
          .map(
              history ->
                  history.isSuccess()
                      ? IdentityVerificationStatus.VERIFIED
                      : IdentityVerificationStatus.FAILED)
          .orElse(IdentityVerificationStatus.NONE);
    }
  }

  @Override
  @Transactional
  public void handleWebhook(Map<String, Object> webhookData) {
    log.info("Handling identity verification webhook: {}", webhookData);

    // V2 API 웹훅 처리
    String eventType = (String) webhookData.get("eventType");

    // 본인인증 관련 이벤트만 처리
    if (!eventType.startsWith("IDENTITY_VERIFICATION_")) {
      log.debug("Ignoring non-identity webhook: {}", eventType);
      return;
    }

    // 데이터 추출
    Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
    Map<String, Object> result = (Map<String, Object>) data.get("identityVerificationResult");

    if (result == null) {
      log.warn("No identity verification result in webhook data");
      return;
    }

    String requestId = (String) result.get("orderId");
    String status = (String) webhookData.get("status");

    IdentityVerificationHistory history =
        verificationHistoryRepository.findByPortOneRequestId(requestId).orElse(null);

    if (history == null) {
      log.warn("No verification history found for request ID: {}", requestId);
      return;
    }

    User user = history.getUser();

    // 상태에 따른 처리
    switch (status) {
      case "READY":
        log.debug("Updating status to REQUESTED for request: {}", requestId);
        history.markAsUpdate(IdentityVerificationStatus.REQUESTED, webhookData);
        break;

      case "IN_PROGRESS":
        log.debug("Updating status to IN_PROGRESS for request: {}", requestId);
        history.markAsUpdate(IdentityVerificationStatus.IN_PROGRESS, webhookData);
        break;

      case "COMPLETED":
      case "DONE":
        log.info("Received completed webhook for request: {}", requestId);
        // 인증 완료 처리
        if (history.isSuccess()) {
          log.debug("Request already processed successfully: {}", requestId);
        } else {
          log.info("Processing verification result from webhook");

          // 인증 토큰 추출
          String authToken = (String) result.get("authToken");
          if (authToken != null) {
            processVerificationResult(authToken, null);
          } else {
            log.warn("No auth token in webhook data, cannot process verification");
            history.markAsUpdate(IdentityVerificationStatus.VERIFIED, webhookData);
          }
        }
        break;

      case "FAILED":
        log.warn("Verification failed for request: {}", requestId);
        Map<String, Object> error = (Map<String, Object>) result.get("error");
        String failCode = error != null ? (String) error.get("code") : "UNKNOWN";
        String failMessage = error != null ? (String) error.get("message") : "Unknown error";

        history.markAsFailure(
            IdentityVerificationStatus.FAILED, failCode, failMessage, webhookData);
        break;

      case "EXPIRED":
        log.warn("Verification expired for request: {}", requestId);
        history.markAsFailure(
            IdentityVerificationStatus.EXPIRED,
            "EXPIRED",
            "Verification request expired",
            webhookData);
        break;

      default:
        log.warn("Unknown verification status: {}", status);
    }

    verificationHistoryRepository.save(history);
  }
}
