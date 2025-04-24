package liaison.groble.api.model.payment.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentPrepareResponse {
  private String paymentKey;
  private String merchantUid;
  private BigDecimal amount;
  private String status;
  private String pgProvider;
  private String clientKey;
}
