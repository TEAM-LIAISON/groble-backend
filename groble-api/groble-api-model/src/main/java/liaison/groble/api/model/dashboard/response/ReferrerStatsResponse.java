package liaison.groble.api.model.dashboard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 대시보드 - 콘텐츠 상세 유입경로 응답] 콘텐츠 상세 유입경로 응답")
public class ReferrerStatsResponse {
  private String referrerUrl;
  private Long visitCount;
}
