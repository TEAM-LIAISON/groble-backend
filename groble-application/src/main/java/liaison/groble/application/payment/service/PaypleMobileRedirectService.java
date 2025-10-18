package liaison.groble.application.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.exception.PaypleMobileRedirectException;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.order.entity.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaypleMobileRedirectService {

  private static final String PAYMENT_RESULT_PATH = "payment-result";

  private final OrderReader orderReader;

  @Value("${app.frontend-url}")
  private String frontendBaseUrl;

  public String buildSuccessRedirectUrl(String merchantUid, boolean isSuccess) {
    try {
      Order order = orderReader.getOrderByMerchantUid(merchantUid);
      Long contentId = extractPrimaryContentId(order);
      return baseUriBuilder()
          .pathSegment("products", contentId.toString(), PAYMENT_RESULT_PATH)
          .queryParam("merchantUid", merchantUid)
          .queryParam("success", isSuccess)
          .build()
          .toUriString();
    } catch (EntityNotFoundException ex) {
      throw PaypleMobileRedirectException.orderNotFound(merchantUid, ex);
    } catch (IllegalStateException ex) {
      throw PaypleMobileRedirectException.orderContentMissing(merchantUid, ex);
    } catch (PaypleMobileRedirectException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      throw PaypleMobileRedirectException.unexpected(ex);
    }
  }

  public String buildFailureRedirectUrl(String merchantUid, String errorMessage) {
    String message =
        (errorMessage == null || errorMessage.isBlank())
            ? PaypleMobileRedirectException.defaultClientMessage()
            : errorMessage;

    return baseUriBuilder()
        .pathSegment("products", PAYMENT_RESULT_PATH)
        .queryParam("merchantUid", merchantUid)
        .queryParam("success", false)
        .queryParam("error", message)
        .build()
        .toUriString();
  }

  public boolean isPaymentSuccess(String payResult) {
    return "success".equalsIgnoreCase(payResult);
  }

  private Long extractPrimaryContentId(Order order) {
    return order.getOrderItems().stream()
        .findFirst()
        .map(orderItem -> orderItem.getContent().getId())
        .orElseThrow(() -> new IllegalStateException("주문에 콘텐츠가 없습니다."));
  }

  private UriComponentsBuilder baseUriBuilder() {
    String normalizedBaseUrl = normalizedFrontendBaseUrl();
    return UriComponentsBuilder.fromUriString(normalizedBaseUrl);
  }

  private String normalizedFrontendBaseUrl() {
    if (frontendBaseUrl == null || frontendBaseUrl.isBlank()) {
      throw new IllegalStateException("프론트엔드 URL이 설정되지 않았습니다.");
    }
    return frontendBaseUrl.endsWith("/")
        ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
        : frontendBaseUrl;
  }
}
