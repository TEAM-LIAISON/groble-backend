package liaison.groble.application.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderSuccessDTO {
  private final String merchantUid;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private final LocalDateTime purchasedAt;

  private final Long contentId;

  private final String contentTitle;

  private final String paymentType;

  private final LocalDate nextPaymentDate;

  private final String cardName;

  private final String cardNumberLast4;

  private final String sellerName;

  private final String orderStatus;

  private final String contentDescription;
  private final String contentThumbnailUrl;

  private final Long selectedOptionId;
  private final String selectedOptionType;
  private final String selectedOptionName;

  private final BigDecimal originalPrice;
  private final BigDecimal discountPrice;
  private final BigDecimal finalPrice;

  private final Boolean isFreePurchase;
}
