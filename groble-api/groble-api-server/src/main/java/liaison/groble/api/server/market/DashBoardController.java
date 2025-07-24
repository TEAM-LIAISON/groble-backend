package liaison.groble.api.server.market;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
@Tag(
    name = "[📈 대시보드] 수익, 조회수, 고객수 등 통계 정보 제공",
    description = "판매자 대시보드에서 수익, 조회수, 고객수 등의 통계 정보를 제공합니다.")
public class DashBoardController {

  // TODO: GET - 대시보드 통계 요약 정보 (총 수익, N월 수익, 조회수, 고객수)
  // TODO: GET - 대시보드 홈화면에서 내 콘텐츠 제목 데이터 (페이지네이션)
  // TODO: GET - 대시보드 홈화면에서 판매 알림 상위 5개 데이터만 조회 (가격, 날짜(최신순))

  // TODO: GET - 대시보드 > 조회수 (마켓 조회수, 콘텐츠 조회수, 개별 콘텐츠에 대한 조회수 with 페이지네이션) with 오늘, 지난 7일, 최근 30일, 이번
  // 달, 지난 달

  // TODO: GET - 대시보드 - 마켓 조회수 & 유입 경로 (오늘, 지난 7일, 최근 30일, 이번 달, 지난 달)

  // TODO: GET - 대시보드 - 콘텐츠 개별 조회수

}
