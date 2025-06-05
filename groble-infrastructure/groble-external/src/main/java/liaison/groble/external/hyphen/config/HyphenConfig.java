package liaison.groble.external.hyphen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "hyphen")
public class HyphenConfig {

  private String apiUrl = "https://api.hyphen.im";
  private String userId;
  private String hkey;
  private boolean testMode = true; // 테스트 모드 여부

  // API Endpoints
  public String getAccountVerificationUrl() {
    return apiUrl + "/hb0081000398";
  }

  public String getOneWonTransferUrl() {
    return apiUrl + "/hb0081000378";
  }

  public String getOneWonVerifyUrl() {
    return apiUrl + "/hb0081000379";
  }

  // 테스트 모드일 때 gustation 헤더 값
  public String getGustationHeader() {
    return testMode ? "Y" : null;
  }
}
