package liaison.groble.external.sms;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SmsRequest {
  String type; // SMS Type (SMS, LMS, MMS)
  String contentType; // 메시지 Type (COMM, AD)
  String countryCode; // 국가 번호
  String from; // 발신자
  String content; // 기본 메시지 내용
  List<Message> messages;
}
