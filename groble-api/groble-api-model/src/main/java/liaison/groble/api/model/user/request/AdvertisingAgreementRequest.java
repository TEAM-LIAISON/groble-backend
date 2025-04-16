package liaison.groble.api.model.user.request;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;

@Getter
public class AdvertisingAgreementRequest {
  @NotNull(message = "광고 수신 동의 여부는 필수입니다.")
  private Boolean agreed;
}
