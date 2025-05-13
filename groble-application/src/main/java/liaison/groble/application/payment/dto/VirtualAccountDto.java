package liaison.groble.application.payment.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualAccountDto {
  private Long orderId;
  private Map<String, Object> bankInfo;
}
