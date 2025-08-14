package liaison.groble.api.server.dashboard;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Tag(name = "[📊 대시보드 조회 API]", description = "수익, 조회수, 고객수, 콘텐츠 목록 등 조회 API")
public class DashboardController {
  // TODO(1): 대시보드 홈화면 개요 [메이커 인증 여부, 총 수익(수익, 건수), N월 수익(수익, 건수), 조회수(마켓, 콘텐츠), 고객수(전체, 신규 -> 최근
  // 30일 기준 신규 구매자)]
  // TODO(2): 내 콘텐츠 전체 목록 조회 (20개씩, 최신순 정렬 페이징)

  // TODO(3): 오늘/지난 7일/최근 30일/이번 달/지난 달 선택에 따른 마켓과 콘텐츠 조회 [콘텐츠는 목록 제공]

  // TODO(4): 오늘/지난 7일/최근 30일/이번 달/지난 달 선택에 따른 마켓 상세 조회수 제공 + 유입 경로 제공
  // TODO(5): 오늘/지난 7일/최근 30일/이번 달/지난 달 선택에 따른 콘텐츠 상세 조회수 제공 + 유입 경로 제공
}
