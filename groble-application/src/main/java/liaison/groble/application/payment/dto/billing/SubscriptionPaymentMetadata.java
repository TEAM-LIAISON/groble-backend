package liaison.groble.application.payment.dto.billing;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPaymentMetadata {
  private final BillingKeyAction billingKeyAction;
  private final boolean hasActiveBillingKey;
  private final String billingKeyId;
  private final String merchantUserKey;
  private final String defaultPayMethod;
  private final String payWork;
  private final String cardVer;
  private final String regularFlag;
  private final LocalDate nextPaymentDate;
  private final String payYear;
  private final String payMonth;
  private final String payDay;
  private final boolean requiresImmediateCharge;
}
