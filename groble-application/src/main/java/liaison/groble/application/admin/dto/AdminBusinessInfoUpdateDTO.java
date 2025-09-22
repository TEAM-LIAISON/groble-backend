package liaison.groble.application.admin.dto;

import liaison.groble.domain.user.enums.BusinessType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBusinessInfoUpdateDTO {
  private BusinessType businessType;
  private String businessName;
  private String representativeName;
  private String businessAddress;
}
