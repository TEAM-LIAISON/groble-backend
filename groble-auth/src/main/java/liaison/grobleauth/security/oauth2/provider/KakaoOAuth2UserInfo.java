package liaison.grobleauth.security.oauth2.provider;

import java.util.Map;

import liaison.grobleauth.security.oauth2.OAuth2UserInfo;

/** Kakao OAuth2 사용자 정보를 처리하는 클래스 */
public class KakaoOAuth2UserInfo extends OAuth2UserInfo {
  public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    return String.valueOf(attributes.get("id"));
  }

  @Override
  public String getEmail() {
    Map<String, Object> kakaoAccount = getKakaoAccount();
    if (kakaoAccount == null) {
      return null;
    }
    return (String) kakaoAccount.get("email");
  }

  @Override
  public String getName() {
    Map<String, Object> kakaoAccount = getKakaoAccount();
    if (kakaoAccount == null) {
      return null;
    }

    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
    if (profile == null) {
      return null;
    }

    return (String) profile.get("nickname");
  }

  @Override
  public String getImageUrl() {
    Map<String, Object> kakaoAccount = getKakaoAccount();
    if (kakaoAccount == null) {
      return null;
    }

    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
    if (profile == null) {
      return null;
    }

    return (String) profile.get("profile_image_url");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getProperties() {
    return (Map<String, Object>) attributes.get("properties");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getKakaoAccount() {
    return (Map<String, Object>) attributes.get("kakao_account");
  }
}
