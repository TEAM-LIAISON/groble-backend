package liaison.groble.api.server.common.swagger;

import liaison.groble.api.model.payment.response.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 결제 관련 API 응답 스키마 클래스
 *
 * <p>Swagger 문서화를 위한 결제 전용 응답 스키마들을 정의합니다.
 */
public final class PaymentResponseSchemas {

  private PaymentResponseSchemas() {}

  /** 결제 승인 응답 스키마 */
  @Schema(description = "결제 승인 응답")
  public static class ApiPaymentRequestResponse
      extends GrobleResponse<AppCardPayplePaymentResponse> {

    @Override
    @Schema(description = "결제 승인 결과 데이터", implementation = AppCardPayplePaymentResponse.class)
    public AppCardPayplePaymentResponse getData() {
      return super.getData();
    }
  }

  /** 결제 취소 응답 스키마 */
  @Schema(description = "결제 취소 응답")
  public static class ApiPaymentCancelResponse extends GrobleResponse<PaymentCancelResponse> {

    @Override
    @Schema(description = "결제 취소 결과 데이터", implementation = PaymentCancelResponse.class)
    public PaymentCancelResponse getData() {
      return super.getData();
    }
  }
}
