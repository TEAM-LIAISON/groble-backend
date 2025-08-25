package liaison.groble.application.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuestOrderSearchRequestDTO {
  private String merchantUid;
  private String phoneNumber;
}
