package liaison.groble.external.sms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Message {
  String to; // 수신자
  String content; // 개별 메시지 내용
}
