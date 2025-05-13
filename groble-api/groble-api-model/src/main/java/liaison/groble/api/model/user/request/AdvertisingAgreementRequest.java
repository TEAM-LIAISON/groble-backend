package liaison.groble.api.model.user.request;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisingAgreementRequest {
  @NotNull(message = "광고 수신 동의 여부는 필수입니다.")
  private Boolean agreed;
}
