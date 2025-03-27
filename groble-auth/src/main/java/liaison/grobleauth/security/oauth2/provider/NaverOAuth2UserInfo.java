package liaison.grobleauth.security.oauth2.provider;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.grobleauth.security.oauth2.OAuth2UserInfo;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {
  private static final Logger logger = LoggerFactory.getLogger(NaverOAuth2UserInfo.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @SuppressWarnings("unchecked")
  public NaverOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
    logNaverResponse();
  }

  private void logNaverResponse() {
    try {
      logger.info("Naver 전체 attributes: {}", objectMapper.writeValueAsString(attributes));

      Map<String, Object> response = getResponse();
      if (response != null) {
        logger.info("Naver response 객체: {}", objectMapper.writeValueAsString(response));
      }
    } catch (Exception e) {
      logger.error("Naver 응답 로깅 중 오류 발생", e);
    }
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
