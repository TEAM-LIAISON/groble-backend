package liaison.groble.application.maker.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MakerInfoDTO {
  private final MakerType makerType;
  private final boolean verified;
  private final String verificationStatus;
  private final String verificationStatusLabel;
  private final String name;
  private final String email;
  private final String phoneNumber;
  private final BusinessMakerInfoDTO businessInfo;
}
