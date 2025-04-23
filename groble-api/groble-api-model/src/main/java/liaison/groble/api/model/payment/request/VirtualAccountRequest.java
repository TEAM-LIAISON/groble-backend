package liaison.groble.api.model.payment.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualAccountRequest {

  @NotNull(message = "주문 ID는 필수입니다.")
  private Long orderId;

  @NotBlank(message = "은행 코드는 필수입니다.")
  private String bankCode;

  @NotNull(message = "만료일은 필수입니다.")
  @Future(message = "만료일은 미래 날짜여야 합니다.")
  private LocalDateTime dueDate;
}
