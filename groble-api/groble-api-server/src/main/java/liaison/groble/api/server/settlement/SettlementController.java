package liaison.groble.api.server.settlement;

import java.time.YearMonth;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.settlement.response.PerTransactionSettlementOverviewResponse;
import liaison.groble.api.model.settlement.response.SettlementDetailResponse;
import liaison.groble.api.model.settlement.response.SettlementOverviewResponse;
import liaison.groble.api.model.settlement.response.SettlementsOverviewResponse;
import liaison.groble.api.model.settlement.response.TaxInvoiceResponse;
import liaison.groble.api.model.settlement.response.swagger.MonthlySettlementOverviewListResponse;
import liaison.groble.api.model.settlement.response.swagger.PerTransactionSettlementOverviewListResponse;
import liaison.groble.application.settlement.dto.PerTransactionSettlementOverviewDTO;
import liaison.groble.application.settlement.dto.SettlementDetailDTO;
import liaison.groble.application.settlement.dto.SettlementOverviewDTO;
import liaison.groble.application.settlement.dto.SettlementsOverviewDTO;
import liaison.groble.application.settlement.dto.TaxInvoiceDTO;
import liaison.groble.application.settlement.service.SettlementService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.settlement.SettlementMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Tag(name = "[💰 정산 관리 조회 API]", description = "정산 관리, 정산 내역 상세 조회 API 등")
public class SettlementController {
  // API 경로 상수화
  private static final String SETTLEMENT_OVERVIEW_PATH = "/settlements/overview";
  private static final String SETTLEMENT_LIST_PATH = "/settlements";
  private static final String SETTLEMENT_DETAIL_PATH = "/settlements/{settlementId}";
  private static final String SALES_LIST_PATH = "/settlements/sales/{settlementId}";
  private static final String TAX_INVOICE_PATH = "/settlements/tax-invoice/{yearMonth}";

  // 응답 메시지 상수화
  private static final String SETTLEMENT_OVERVIEW_SUCCESS_MESSAGE = "정산 개요 조회 성공";
  private static final String SETTLEMENT_LIST_SUCCESS_MESSAGE = "정산 내역 전체 조회 성공";
  private static final String SETTLEMENT_DETAIL_SUCCESS_MESSAGE = "정산 상세 내역 조회 성공";
  private static final String SALES_LIST_SUCCESS_MESSAGE = "총 판매 내역 조회 성공";
  private static final String TAX_INVOICE_SUCCESS_MESSAGE = "세금계산서 발행 성공";

  // Service
  private final SettlementService settlementService;
  // Mapper
  private final SettlementMapper settlementMapper;
  // Helper
  private final ResponseHelper responseHelper;

  // TODO: (1) 정산 개요 조회 (메이커 인증 여부 / 누적 정산 금액 / 정산 예정 금액)
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[💰 정산 개요 조회] 정산 개요 조회",
      description = "메이커 인증 여부, 누적 정산 금액, 정산 예정 금액을 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = SETTLEMENT_OVERVIEW_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SettlementOverviewResponse.class)))
  @GetMapping(SETTLEMENT_OVERVIEW_PATH)
  @Logging(
      item = "Settlement",
      action = "getSettlementOverview",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<SettlementOverviewResponse>> getSettlementOverview(
      @Auth Accessor accessor) {
    SettlementOverviewDTO settlementOverviewDTO =
        settlementService.getSettlementOverview(accessor.getUserId());
    SettlementOverviewResponse settlementOverviewResponse =
        settlementMapper.toSettlementOverviewResponse(settlementOverviewDTO);
    return responseHelper.success(
        settlementOverviewResponse, SETTLEMENT_OVERVIEW_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // TODO: (2) 정산 내역 전체 조회 (페이징) - 월별 정산 상태, 정산 금액 제공
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[💰 정산 내역 전체 조회] 정산 내역 전체 조회",
      description = "정산 내역을 월별로 조회합니다. 페이지네이션 및 정렬 기능을 지원합니다.")
  @ApiResponse(
      responseCode = "200",
      description = SETTLEMENT_LIST_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = MonthlySettlementOverviewListResponse.class)))
  @GetMapping(SETTLEMENT_LIST_PATH)
  @Logging(
      item = "Settlement",
      action = "getSettlementList",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<PageResponse<SettlementsOverviewResponse>>>
      getSettlementList(
          @Auth Accessor accessor,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "20") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt") String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<SettlementsOverviewDTO> dtoPage =
        settlementService.getMonthlySettlements(accessor.getUserId(), pageable);
    PageResponse<SettlementsOverviewResponse> responsePage =
        settlementMapper.toMonthlySettlementOverviewResponsePage(dtoPage);

    return responseHelper.success(responsePage, SETTLEMENT_LIST_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // TODO: (3) 정산 상세 내역 조회 (월 요약 정보 / 세금계산서 다운로드 가능 여부)
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[💰 정산 상세 내역 조회] 정산 상세 내역 조회",
      description = "월별 정산 상세 내역 조회, 세금계산서 다운로드 가능 여부를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = SETTLEMENT_DETAIL_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SettlementDetailResponse.class)))
  @GetMapping(SETTLEMENT_DETAIL_PATH)
  @Logging(
      item = "Settlement",
      action = "getSettlementDetail",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<SettlementDetailResponse>> getSettlementDetail(
      @Auth Accessor accessor,
      @Parameter(
              name = "settlementId",
              description = "숫자 형식",
              example = "265",
              schema = @Schema(type = "number"))
          @PathVariable("settlementId")
          Long settlementId) {

    SettlementDetailDTO settlementDetailDTO =
        settlementService.getSettlementDetail(accessor.getUserId(), settlementId);
    SettlementDetailResponse settlementDetailResponse =
        settlementMapper.toSettlementResponse(settlementDetailDTO);
    return responseHelper.success(
        settlementDetailResponse, SETTLEMENT_DETAIL_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // TODO: (4) 총 판매 내역 조회 (페이징) - 콘텐츠별 정산 금액, 콘텐츠별 판매 시각
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[💰 정산 총 판매 내역 조회] 총 판매 내역 조회",
      description = "총 판매 내역을 콘텐츠별로 조회합니다. 페이지네이션 및 정렬 기능을 지원합니다.")
  @ApiResponse(
      responseCode = "200",
      description = SALES_LIST_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema =
                  @Schema(implementation = PerTransactionSettlementOverviewListResponse.class)))
  @GetMapping(SALES_LIST_PATH)
  @Logging(item = "Settlement", action = "getSalesList", includeParam = true, includeResult = true)
  public ResponseEntity<GrobleResponse<PageResponse<PerTransactionSettlementOverviewResponse>>>
      getSalesList(
          @Auth Accessor accessor,
          @Parameter(
                  name = "settlementId",
                  description = "숫자 형식",
                  example = "265",
                  schema = @Schema(type = "number"))
              @PathVariable("settlementId")
              Long settlementId,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "20") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt") String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<PerTransactionSettlementOverviewDTO> dtoPage =
        settlementService.getPerTransactionSettlements(
            accessor.getUserId(), settlementId, pageable);
    PageResponse<PerTransactionSettlementOverviewResponse> responsePage =
        settlementMapper.toPerTransactionSettlementOverviewResponsePage(dtoPage);

    return responseHelper.success(responsePage, SALES_LIST_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // 세금계산서 발행 받기
  @Deprecated
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[💰 세금계산서 상세 정보 조회] 세금계산서 데이터를 조회합니다.",
      description = "월별 세금계산서 상세 데이터를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = TAX_INVOICE_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = TaxInvoiceResponse.class)))
  @GetMapping(TAX_INVOICE_PATH)
  @Logging(item = "Settlement", action = "getTaxInvoice", includeParam = true, includeResult = true)
  public ResponseEntity<GrobleResponse<TaxInvoiceResponse>> getTaxInvoice(
      @Auth Accessor accessor,
      @Parameter(
              name = "yearMonth",
              description = "yyyy-MM 형식",
              example = "2025-08",
              schema = @Schema(type = "string", pattern = "^\\d{4}-(0[1-9]|1[0-2])$"))
          @PathVariable("yearMonth")
          YearMonth yearMonth) {

    TaxInvoiceDTO taxInvoiceDTO = settlementService.getTaxInvoice(accessor.getUserId(), yearMonth);

    TaxInvoiceResponse taxInvoiceResponse = settlementMapper.toTaxInvoiceResponse(taxInvoiceDTO);

    return responseHelper.success(taxInvoiceResponse, TAX_INVOICE_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
