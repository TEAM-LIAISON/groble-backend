package liaison.groble.application.order.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateOrderSuccessDTO {
  private String merchantUid;
  private String email;
  private String phoneNumber;
  private String contentTitle;
  private BigDecimal totalPrice;
  private Boolean isPurchasedContent;
}
