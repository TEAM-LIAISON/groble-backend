package liaison.groble.external.infotalk.dto.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 비즈뿌리오 메시지 타입 정의
 *
 * <p>각 메시지 타입별로 다른 특성과 제약사항이 있습니다: - SMS: 90바이트 제한 (한글 45자) - LMS: 2000바이트 제한 (한글 1000자) - MMS:
 * 2000바이트 + 이미지 첨부 가능 - AT: 알림톡 (템플릿 기반, 광고성 메시지 불가) - FT: 친구톡 (템플릿 선택적, 광고성 메시지 가능)
 */
@Getter
@RequiredArgsConstructor
public enum MessageType {
  SMS("SMS", "단문 메시지", 90),
  LMS("LMS", "장문 메시지", 2000),
  MMS("MMS", "멀티미디어 메시지", 2000),
  ALIMTALK("AT", "알림톡", 1000),
  FRIENDTALK("FT", "친구톡", 1000);

  private final String code;
  private final String description;
  private final int maxBytes;

  /**
   * 메시지 내용의 바이트 수를 계산하고 유효성을 검증합니다
   *
   * @param content 메시지 내용
   * @return 유효한 경우 true
   */
  public boolean isValidContent(String content) {
    if (content == null) return false;
    return content.getBytes().length <= maxBytes;
  }
}
