package liaison.groble.application.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.exception.PaypleMobileRedirectException;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.factory.UserContextFactory;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.Order.OrderStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaypleMobileRedirectService {

  private static final String PAYMENT_RESULT_PATH = "payment-result";

  private final OrderReader orderReader;

  @Value("${app.frontend-url}")
  private String frontendBaseUrl;

  public MobileRedirectContext loadContext(String merchantUid) {
    try {
      Order order = orderReader.getOrderByMerchantUid(merchantUid);
      Long contentId = extractPrimaryContentId(order);
      Long userId = order.getUser() != null ? order.getUser().getId() : null;
      Long guestUserId = order.getGuestUser() != null ? order.getGuestUser().getId() : null;

      if (userId == null && guestUserId == null) {
        throw PaypleMobileRedirectException.orderUserMissing(merchantUid, null);
      }

      return new MobileRedirectContext(
          merchantUid, contentId, userId, guestUserId, order.getStatus());
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

  public UserContext buildUserContext(MobileRedirectContext context) {
    try {
      if (context.isMemberOrder()) {
        return UserContextFactory.createMemberContext(context.getUserId());
      }
      if (context.isGuestOrder()) {
        return UserContextFactory.createGuestContext(context.getGuestUserId());
      }
      throw new IllegalStateException("주문에 사용자 정보가 없습니다.");
    } catch (IllegalArgumentException | IllegalStateException ex) {
      throw PaypleMobileRedirectException.orderUserMissing(context.getMerchantUid(), ex);
    }
  }

  public String buildSuccessRedirectUrl(String merchantUid, boolean isSuccess) {
    MobileRedirectContext context = loadContext(merchantUid);
    return buildSuccessRedirectUrl(context, isSuccess);
  }

  public String buildSuccessRedirectUrl(MobileRedirectContext context, boolean isSuccess) {
    return baseUriBuilder()
        .pathSegment("products", context.getContentId().toString(), PAYMENT_RESULT_PATH)
        .queryParam("merchantUid", context.getMerchantUid())
        .queryParam("success", isSuccess)
        .build()
        .toUriString();
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

  public boolean isAlreadyProcessed(MobileRedirectContext context) {
    return context.getOrderStatus() == OrderStatus.PAID;
  }

  public boolean isProcessableStatus(MobileRedirectContext context) {
    return context.getOrderStatus() == OrderStatus.PENDING;
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

  @Getter
  @RequiredArgsConstructor
  public static class MobileRedirectContext {
    private final String merchantUid;
    private final Long contentId;
    private final Long userId;
    private final Long guestUserId;
    private final OrderStatus orderStatus;

    public boolean isMemberOrder() {
      return userId != null;
    }

    public boolean isGuestOrder() {
      return guestUserId != null;
    }
  }
}
