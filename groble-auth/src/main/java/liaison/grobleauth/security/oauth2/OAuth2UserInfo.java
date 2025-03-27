package liaison.grobleauth.security.oauth2;

import java.util.Map;

/** OAuth2 사용자 정보를 추상화한 클래스 각 소셜 로그인 제공자(Google, Kakao, Naver 등)에 맞게 구현됩니다. */
public abstract class OAuth2UserInfo {
  protected Map<String, Object> attributes;

  public OAuth2UserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  /** 원본 속성 맵을 반환합니다. */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /** 소셜 로그인 제공자의 사용자 ID를 반환합니다. */
  public abstract String getId();

  /** 사용자 이메일을 반환합니다. */
  public abstract String getEmail();
}
