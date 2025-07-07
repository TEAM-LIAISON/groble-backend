package liaison.groble.application.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOrderCancellationReasonDTO {
  private String cancelReason;
}
