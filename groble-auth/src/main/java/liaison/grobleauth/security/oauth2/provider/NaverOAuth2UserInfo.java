package liaison.grobleauth.security.oauth2.provider;

import java.util.Map;

import liaison.grobleauth.security.oauth2.OAuth2UserInfo;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {
  @SuppressWarnings("unchecked")
  public NaverOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    Map<String, Object> response = getResponse();
    if (response == null) {
      return null;
    }
    return (String) response.get("id");
  }

  @Override
  public String getEmail() {
    Map<String, Object> response = getResponse();
    if (response == null) {
      return null;
    }
    return (String) response.get("email");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getResponse() {
    return (Map<String, Object>) attributes.get("response");
  }
}
