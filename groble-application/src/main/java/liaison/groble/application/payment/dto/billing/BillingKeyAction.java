package liaison.groble.application.payment.dto.billing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines how the client should handle Payple billing keys for a subscription purchase.
 *
 * <p>The value maps directly to Payple's {@code PCD_PAY_WORK} parameter.
 */
@Getter
@RequiredArgsConstructor
public enum BillingKeyAction {
  /** Reuse an already registered billing key and trigger a direct charge. */
  REUSE("PAY"),
  /** Register a new billing key without charging immediately. */
  REGISTER("LINKREG"),
  /** Register a new billing key and charge the first payment immediately. */
  REGISTER_AND_CHARGE("CERT");

  private final String payWork;
}
