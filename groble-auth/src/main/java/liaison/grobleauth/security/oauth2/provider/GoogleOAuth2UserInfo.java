package liaison.grobleauth.security.oauth2.provider;

import java.util.Map;

import liaison.grobleauth.security.oauth2.OAuth2UserInfo;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {
  public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    return (String) attributes.get("sub");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }
}
