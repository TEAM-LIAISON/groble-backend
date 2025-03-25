package liaison.grobleauth.security.oauth2;

import java.util.Map;

import liaison.grobleauth.exception.OAuth2AuthenticationProcessingException;
import liaison.grobleauth.security.oauth2.provider.KakaoOAuth2UserInfo;

/** OAuth2 사용자 정보 객체를 생성하는 팩토리 클래스 */
public class OAuth2UserInfoFactory {

  /**
   * 소셜 로그인 제공자에 맞는 OAuth2UserInfo 구현체를 반환합니다.
   *
   * @param registrationId 소셜 로그인 제공자 ID (google, kakao, naver)
   * @param attributes 소셜 로그인 사용자 속성 맵
   * @return OAuth2UserInfo 구현체
   */
  public static OAuth2UserInfo getOAuth2UserInfo(
      String registrationId, Map<String, Object> attributes) {
    return switch (registrationId.toLowerCase()) {
        //      case "google" -> new GoogleOAuth2UserInfo(attributes);
      case "kakao" -> new KakaoOAuth2UserInfo(attributes);
        //      case "naver" -> new NaverOAuth2UserInfo(attributes);
      default -> throw new OAuth2AuthenticationProcessingException(
          "죄송합니다. " + registrationId + " 소셜 로그인은 지원하지 않습니다.");
    };
  }
}
