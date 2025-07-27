package liaison.groble.application.market.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketViewCountDTO {
  private Long userId; // 로그인 사용자 ID
  private String ip; // IP 주소
  private String userAgent; // User-Agent
  private String referer; // 유입 경로
}
