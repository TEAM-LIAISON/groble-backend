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
  private PaypleOptionsDTO paypleOptions;

  @Getter
  @Builder
  public static class PaypleOptionsDTO {
    private String billingKeyAction;
    private String payWork;
    private String cardVer;
    private String regularFlag;
    private String defaultPayMethod;
    private String merchantUserKey;
    private String billingKeyId;
    private java.time.LocalDate nextPaymentDate;
    private String payYear;
    private String payMonth;
    private String payDay;
  }
}
