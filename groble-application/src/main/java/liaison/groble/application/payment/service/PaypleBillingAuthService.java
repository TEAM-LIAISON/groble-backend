package liaison.groble.application.payment.service;

import org.springframework.stereotype.Service;

import liaison.groble.application.payment.dto.PaypleAuthResponseDTO;
import liaison.groble.application.payment.exception.PaypleApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 페이플 빌링(정기결제) 인증 서비스
 *
 * <p>MO(리다이렉트) 방식과 API 호출(절대경로) 방식에 따라 파트너 인증을 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaypleBillingAuthService {

  private final PaypleApiClient paypleApiClient;

  /**
   * MO(리다이렉트) 방식으로 페이플 파트너 인증을 요청합니다.
   *
   * @return 페이플 인증 응답
   */
  public PaypleAuthResponseDTO requestMoAuth() {
    return requestBillingAuth(PaypleBillingAuthMode.MO);
  }

  /**
   * API(절대경로) 방식으로 페이플 파트너 인증을 요청합니다.
   *
   * @return 페이플 인증 응답
   */
  public PaypleAuthResponseDTO requestApiAuth() {
    return requestBillingAuth(PaypleBillingAuthMode.API);
  }

  private PaypleAuthResponseDTO requestBillingAuth(PaypleBillingAuthMode mode) {
    try {
      log.info("페이플 빌링 인증 요청 - mode: {}, payWork: {}", mode.name(), mode.getPayWork());
      return paypleApiClient.requestAuth(mode.getPayWork());
    } catch (PaypleApiException e) {
      throw e;
    } catch (Exception e) {
      log.error("페이플 빌링 인증 중 예상치 못한 오류 - mode: {}", mode.name(), e);
      throw new PaypleApiException("페이플 빌링 인증 요청 실패", e);
    }
  }
}
