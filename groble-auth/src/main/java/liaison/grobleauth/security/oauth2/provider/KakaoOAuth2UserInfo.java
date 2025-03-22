package liaison.grobleauth.security.oauth2.provider;

import java.util.Map;

import liaison.grobleauth.security.oauth2.OAuth2UserInfo;

public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

  public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    return String.valueOf(attributes.get("id"));
  }

  @Override
  public String getName() {
    Map<String, Object> properties = getProperties();
    if (properties == null) {
      return null;
    }
    return (String) properties.get("nickname");
  }

  @Override
  public String getEmail() {
    Map<String, Object> kakaoAccount = getKakaoAccount();
    if (kakaoAccount == null) {
      return null;
    }
    return (String) kakaoAccount.get("email");
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
