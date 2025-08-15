package liaison.groble.api.model.dashboard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 대시보드 - 메인 개요] 대시보드 메인 개요 응답")
public class DashboardOverviewResponse {
  // 메이커 인증 여부

  // 전체 총 수익
  // 전체 총 판매 건수

  // N월 총 수익
  // N월 총 판매 건수

  // 마켓 전체 조회수
  // 콘텐츠 전체 조회수

  // 고객 전체 조회수
  // 고객 신규 조회수 (최근 30일 기준)
}
