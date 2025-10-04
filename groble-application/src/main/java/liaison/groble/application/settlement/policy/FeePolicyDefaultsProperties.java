package liaison.groble.application.settlement.policy;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "groble.fee-policy.defaults")
public class FeePolicyDefaultsProperties {

  private BigDecimal platformFeeRateApplied = new BigDecimal("0.0150");
  private BigDecimal platformFeeRateDisplay = new BigDecimal("0.0150");
  private BigDecimal platformFeeRateBaseline = new BigDecimal("0.0150");
  private BigDecimal pgFeeRateApplied = new BigDecimal("0.0290");
  private BigDecimal pgFeeRateDisplay = new BigDecimal("0.0170");
  private BigDecimal pgFeeRateBaseline = new BigDecimal("0.0170");
  private BigDecimal vatRate = new BigDecimal("0.1000");
}
