package liaison.groble.security.oauth2.userinfo;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {
  public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    // 'sub' 필드가 없으면 'id' 필드를 시도
    String sub = (String) attributes.get("sub");
    if (sub != null) {
      return sub;
    }

    // 'id' 필드 확인
    return (String) attributes.get("id");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }

  @Override
  public String getName() {
    return (String) attributes.get("name");
  }

  @Override
  public String getImageUrl() {
    return (String) attributes.get("picture");
  }
}
