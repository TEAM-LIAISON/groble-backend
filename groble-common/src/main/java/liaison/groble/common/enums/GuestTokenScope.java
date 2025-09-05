package liaison.groble.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GuestTokenScope {
  PHONE_VERIFIED("PHONE_VERIFIED", "전화번호 인증 완료 - 제한적 접근"),
  FULL_ACCESS("FULL_ACCESS", "전체 접근 권한 - 이메일/이름 등록 완료");

  private final String code;
  private final String description;

  public static GuestTokenScope fromCode(String code) {
    for (GuestTokenScope scope : values()) {
      if (scope.getCode().equals(code)) {
        return scope;
      }
    }
    return FULL_ACCESS; // 기본값
  }
}
