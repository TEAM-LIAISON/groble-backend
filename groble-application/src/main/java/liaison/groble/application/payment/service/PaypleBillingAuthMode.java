package liaison.groble.application.payment.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 페이플 빌링 인증 모드
 *
 * <p>MO(리다이렉트)와 API(절대경로) 방식별로 Payple API에 전달할 payWork 값을 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum PaypleBillingAuthMode {
  /** MO 방식 (Redirect) */
  MO("LINKREG"),

  /** API 호출 방식 (절대경로) */
  API("AUTH");

  private final String payWork;
}
