package liaison.groble.external.sms;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SmsResponse {
  String requestId; // 요청 아이디
  LocalDateTime requestTime; // 요청 시간
  String statusCode; // 202: 성공, 그 외: 실패
  String statusName; // success: 성공, fail: 실패
}
