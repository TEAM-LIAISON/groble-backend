package liaison.groble.api.server.settlement;

import java.time.YearMonth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.settlement.response.SettlementDetailResponse;
import liaison.groble.application.settlement.dto.SettlementDetailDTO;
import liaison.groble.application.settlement.service.SettlementService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.settlement.SettlementMapper;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class SettlementController {
  // API 경로 상수화
  private static final String SETTLEMENT_OVERVIEW_PATH = "/settlements/overview";
  private static final String SETTLEMENT_LIST_PATH = "/settlements";
  private static final String SETTLEMENT_DETAIL_PATH = "/settlements/{yearMonth}";
  private static final String SALES_LIST_PATH = "/settlements/sales";

  // 응답 메시지 상수화
  private static final String SETTLEMENT_OVERVIEW_SUCCESS_MESSAGE = "정산 개요 조회 성공";
  private static final String SETTLEMENT_LIST_SUCCESS_MESSAGE = "정산 내역 전체 조회 성공";
  private static final String SETTLEMENT_DETAIL_SUCCESS_MESSAGE = "정산 상세 내역 조회 성공";
  private static final String SALES_LIST_SUCCESS_MESSAGE = "총 판매 내역 조회 성공";

  // Service
  private final SettlementService settlementService;
  // Mapper
  private final SettlementMapper settlementMapper;
  // Helper
  private final ResponseHelper responseHelper;

  // TODO: (1) 정산 개요 조회 (메이커 인증 여부 / 누적 정산 금액 / 정산 예정 금액)
  //    @Operation(summary = "[💰 정산] 정산 개요 조회", description = "메이커 인증 여부, 누적 정산 금액, 정산 예정 금액을
  // 조회합니다.")
  //    @GetMapping(SETTLEMENT_OVERVIEW_PATH)
  //    public ResponseEntity<GrobleResponse<Void>> getSettlementOverview(
  //            @Auth Accessor accessor
  //    ) {
  //
  //    }

  // TODO: (2) 정산 내역 전체 조회 (페이징) - 월별 정산 상태, 정산 금액 제공

  // TODO: (3) 정산 상세 내역 조회 (월 요약 정보 / 세금계산서 다운로드 가능 여부)
  @Operation(
      summary = "[💰 정산] 정산 상세 내역 조회",
      description = "월별 정산 상세 내역 조회, 세금계산서 다운로드 가능 여부를 조회합니다.")
  @GetMapping(SETTLEMENT_DETAIL_PATH)
  public ResponseEntity<GrobleResponse<SettlementDetailResponse>> getSettlementDetail(
      @Auth Accessor accessor, @PathVariable("yearMonth") YearMonth yearMonth) {

    SettlementDetailDTO settlementDetailDTO =
        settlementService.getSettlementDetail(accessor.getUserId(), yearMonth);
    SettlementDetailResponse settlementDetailResponse =
        settlementMapper.toSettlementResponse(settlementDetailDTO);
    return responseHelper.success(
        settlementDetailResponse, SETTLEMENT_DETAIL_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // TODO: (4) 총 판매 내역 조회 (페이징) - 콘텐츠별 정산 금액, 콘텐츠별 판매 시각

  // TODO: 세금계산서 다운로드
}
