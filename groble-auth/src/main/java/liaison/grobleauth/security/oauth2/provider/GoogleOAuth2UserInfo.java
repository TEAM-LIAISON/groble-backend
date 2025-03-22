package liaison.grobleauth.security.oauth2.provider;

import java.util.Map;

import liaison.grobleauth.security.oauth2.OAuth2UserInfo;

/** Google OAuth2 사용자 정보를 처리하는 클래스 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

  public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    return (String) attributes.get("sub");
  }

  @Override
  public String getName() {
    return (String) attributes.get("name");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }
}
