package liaison.groble.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleAuthResponseDto {
  private String result; // 인증 결과 (success/error)
  private String resultMsg; // 인증 메시지
  private String cstId; // 파트너 ID (매 인증 시 변동되는 값)
  private String custKey; // 파트너 인증을 위한 키 (외부에 노출되면 안되는 정보)
  private String authKey; // 파트너 인증 키
  private String payWork; // 결제 작업 구분
  private String payUrl; // 결제 URL
  private String returnUrl; // 결제 완료 후 리턴 URL
  private String clientKey; // 클라이언트 키 (기존 호환성)
}
