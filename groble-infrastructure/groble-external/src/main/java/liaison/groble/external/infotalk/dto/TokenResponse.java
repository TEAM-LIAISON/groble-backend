package liaison.groble.external.infotalk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TokenResponse {
  @JsonProperty("accesstoken")
  private String accessToken;

  // 토큰 타입 (일반적으로 "Bearer")
  private String type;

  // 만료시간 (yyyyMMddHHmmss 형식의 문자열)
  // 예: "20201110185520" = 2020년 11월 10일 18시 55분 20초
  private String expired;
}
