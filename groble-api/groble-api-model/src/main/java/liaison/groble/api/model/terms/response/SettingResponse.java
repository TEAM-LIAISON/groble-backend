package liaison.groble.api.model.terms.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingResponse {
  private Boolean isAdvertisingAgreement;
  private Boolean isAllowWithdraw;
}
