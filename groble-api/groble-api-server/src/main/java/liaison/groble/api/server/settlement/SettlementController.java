package liaison.groble.api.server.settlement;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.common.response.ResponseHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class SettlementController {
  // API 경로 상수화
  private static final String SETTLEMENT_OVERVIEW_PATH = "/settlements/overview";

  // 응답 메시지 상수화
  private static final String SETTLEMENT_OVERVIEW_SUCCESS_MESSAGE = "정산 개요 조회 성공";

  // Service

  // Mapper

  // Helper
  private final ResponseHelper responseHelper;

  // TODO: (1) 정산 개요 조회 (메이커 인증 여부 / 누적 정산 금액 / 정산 예정 금액)
  // TODO: (2) 정산 내역 전체 조회 (페이징) - 월별 정산 상태, 정산 금액 제공

  // TODO: (3) 정산 상세 내역 조회 (월 요약 정보 / 세금계산서 다운로드 가능 여부)
  // TODO: (4) 총 판매 내역 조회 (페이징) - 콘텐츠별 정산 금액, 콘텐츠별 판매 시각

  // TODO: 세금계산서 다운로드
}
