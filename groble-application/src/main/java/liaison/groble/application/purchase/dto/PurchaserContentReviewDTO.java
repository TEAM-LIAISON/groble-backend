package liaison.groble.application.purchase.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaserContentReviewDTO {
  private BigDecimal rating;
  private String reviewContent;
}
