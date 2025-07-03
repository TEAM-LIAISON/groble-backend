package liaison.groble.application.payment.dto.cancel;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelInfoDTO {
  private String merchantUid;
  private BigDecimal originalPrice;
  private BigDecimal discountPrice;
  private BigDecimal finalPrice;
  private String payType;
  private String payCardName;
  private String payCardNum;
}
