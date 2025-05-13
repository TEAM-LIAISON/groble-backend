package liaison.groble.application.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultDto {
  private Long id;
  private String paymentKey;
  private String merchantUid;
  private BigDecimal amount;
  private String status;
  private String paymentMethod;
  private String customerName;
  private String customerEmail;
  private String customerPhone;
  private String pgProvider;
  private String clientKey;

  // Card info
  private String cardNumber;
  private String cardIssuerName;
  private String cardAcquirerName;
  private String cardInstallmentPlanMonths;

  // Virtual account info
  private String vbankNumber;
  private String vbankBankName;
  private LocalDateTime vbankExpiryDate;

  // Additional info
  private String receiptUrl;
  private Boolean isEscrow;
  private Boolean isCashReceipt;

  // Cancel info
  private String cancelReason;
  private BigDecimal cancelAmount;
  private Instant canceledAt;
}
