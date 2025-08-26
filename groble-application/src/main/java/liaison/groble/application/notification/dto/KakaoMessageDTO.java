package liaison.groble.application.notification.dto;

import java.util.List;

import liaison.groble.external.infotalk.dto.message.ButtonInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoMessageDTO {
  private String phoneNumber;
  private String templateCode;
  private String title;
  private String content;
  private List<ButtonInfo> buttons;
}
