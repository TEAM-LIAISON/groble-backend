package liaison.groble.api.server.dashboard;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.dashboard.response.ContentOverviewResponse;
import liaison.groble.api.model.dashboard.response.ContentTotalViewStatsResponse;
import liaison.groble.api.model.dashboard.response.ContentViewStatsResponse;
import liaison.groble.api.model.dashboard.response.DashboardOverviewResponse;
import liaison.groble.api.model.dashboard.response.DashboardViewStatsResponse;
import liaison.groble.api.model.dashboard.response.MarketViewStatsResponse;
import liaison.groble.api.model.dashboard.response.ReferrerStatsResponse;
import liaison.groble.api.model.dashboard.response.swagger.ContentOverviewListResponse;
import liaison.groble.application.dashboard.dto.ContentTotalViewStatsDTO;
import liaison.groble.application.dashboard.dto.ContentViewStatsDTO;
import liaison.groble.application.dashboard.dto.DashboardContentOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardViewStatsDTO;
import liaison.groble.application.dashboard.dto.MarketViewStatsDTO;
import liaison.groble.application.dashboard.dto.referrer.ReferrerStatsDTO;
import liaison.groble.application.dashboard.service.DashboardService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.dashboard.DashboardMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Tag(name = "[📊 대시보드 조회 API]", description = "수익, 조회수, 고객수, 콘텐츠 목록 등 조회 API")
public class DashboardController {
  // API 경로 상수화
  private static final String DASHBOARD_OVERVIEW_PATH = "/dashboard/overview";
  private static final String DASHBOARD_CONTENTS_LIST_PATH = "/dashboard/my-contents";
  private static final String DASHBOARD_VIEW_STATS_PATH = "/dashboard/view-stats";
  private static final String DASHBOARD_CONTENT_VIEWS_LIST_PATH = "/dashboard/content/view-stats";
  private static final String DASHBOARD_CONTENT_VIEW_STATS_PATH =
      "/dashboard/content/{contentId}/view-stats";
  private static final String DASHBOARD_CONTENT_REFERRER_STATS_PATH =
      "/dashboard/content/{contentId}/referrer-stats";

  private static final String DASHBOARD_MARKET_VIEW_STATS_PATH =
      "/dashboard/market/{marketId}/view-stats";
  private static final String DASHBOARD_MARKET_REFERRER_STATS_PATH =
      "/dashboard/market/{marketLinkUrl}/referrer-stats";

  // 응답 메시지 상수화
  private static final String DASHBOARD_OVERVIEW_SUCCESS_MESSAGE = "대시보드 개요 조회 성공";
  private static final String DASHBOARD_CONTENTS_LIST_SUCCESS_MESSAGE = "대시보드 내 콘텐츠 전체 목록 조회 성공";
  private static final String DASHBOARD_VIEW_STATS_SUCCESS_MESSAGE = "대시보드 마켓과 콘텐츠 조회수 조회 성공";
  private static final String DASHBOARD_CONTENTS_VIEW_STATS_SUCCESS_MESSAGE =
      "대시보드 콘텐츠 개별 전체 조회수 목록 조회 성공";
  private static final String DASHBOARD_CONTENT_VIEW_STATS_SUCCESS_MESSAGE =
      "대시보드 콘텐츠 날짜별 조회수 조회 성공";
  private static final String DASHBOARD_MARKET_DETAIL_STATS_SUCCESS_MESSAGE =
      "대시보드 마켓 상세 조회수 및 유입 경로 조회 성공";
  private static final String DASHBOARD_CONTENT_REFERRER_STATS_SUCCESS_MESSAGE =
      "대시보드 콘텐츠 유입 경로 조회 성공";
  // Service
  private final DashboardService dashboardService;
  // Mapper
  private final DashboardMapper dashboardMapper;
  // Helper
  private final ResponseHelper responseHelper;

  /* TODO(1): 대시보드 홈화면 개요 [메이커 인증 여부, 총 수익(수익, 건수), N월 수익(수익, 건수), 조회수(마켓, 콘텐츠), 고객수(전체, 신규 -> 최근 30일 기준 신규 구매자)] */
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[📊 대시보드 개요 조회] 대시보드 개요 조회",
      description =
          "메이커 인증 여부, 총 수익(수익, 건수), N월 수익(수익, 건수), 조회수(마켓, 콘텐츠), 고객수(전체, 신규 -> 최근 30일 기준 신규 구매자)를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = DASHBOARD_OVERVIEW_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = DashboardOverviewResponse.class)))
  @GetMapping(DASHBOARD_OVERVIEW_PATH)
  @Logging(
      item = "Dashboard",
      action = "getDashboardOverview",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<DashboardOverviewResponse>> getDashboardOverview(
      @Auth Accessor accessor) {
    DashboardOverviewDTO dashboardOverviewDTO =
        dashboardService.getDashboardOverview(accessor.getUserId());
    DashboardOverviewResponse dashboardOverviewResponse =
        dashboardMapper.toDashboardOverviewResponse(dashboardOverviewDTO);
    return responseHelper.success(
        dashboardOverviewResponse, DASHBOARD_OVERVIEW_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // TODO(2): 내 콘텐츠 전체 목록 조회 (20개씩, 최신순 정렬 페이징)
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[📊 대시보드 내 콘텐츠 목록 조회] 내 콘텐츠 목록 조회",
      description = "전체 콘텐츠 개수와 콘텐츠 ID, 제목을 반환합니다.")
  @ApiResponse(
      responseCode = "200",
      description = DASHBOARD_CONTENTS_LIST_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentOverviewListResponse.class)))
  @GetMapping(DASHBOARD_CONTENTS_LIST_PATH)
  @Logging(
      item = "Dashboard",
      action = "getMyContentsList",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<PageResponse<ContentOverviewResponse>>> getMyContentsList(
      @Auth Accessor accessor,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "sort", defaultValue = "createdAt") String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<DashboardContentOverviewDTO> dtoPage =
        dashboardService.getMyContentsList(accessor.getUserId(), pageable);
    PageResponse<ContentOverviewResponse> responsePage =
        dashboardMapper.toContentOverviewResponsePage(dtoPage);
    return responseHelper.success(
        responsePage, DASHBOARD_CONTENTS_LIST_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // TODO(3): 오늘/지난 7일/최근 30일/이번 달/지난 달 선택에 따른 마켓과 콘텐츠 조회 [콘텐츠는 목록 제공]
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[📊 대시보드 - 마켓/콘텐츠 전체 조회수 조회] 내 마켓과 콘텐츠 전체 조회수 조회",
      description = "전체 기간 안에서 마켓과 콘텐츠 조회수를 반환합니다.")
  @GetMapping(DASHBOARD_VIEW_STATS_PATH)
  @Logging(item = "Dashboard", action = "getViewStats", includeParam = true, includeResult = true)
  public ResponseEntity<GrobleResponse<DashboardViewStatsResponse>> getViewStats(
      @Auth Accessor accessor) {

    DashboardViewStatsDTO dashboardViewStatsDTO =
        dashboardService.getViewStats(accessor.getUserId());

    DashboardViewStatsResponse dashboardViewStatsResponse =
        dashboardMapper.toDashboardViewStatsResponse(dashboardViewStatsDTO);

    return responseHelper.success(
        dashboardViewStatsResponse, DASHBOARD_VIEW_STATS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[📊 대시보드 - 콘텐츠 개별 전체 조회수 목록 조회] 콘텐츠의 전체 조회수 목록 조회",
      description = "조회수 내림차순으로 모든 콘텐츠의 조회수를 반환합니다.")
  @GetMapping(DASHBOARD_CONTENT_VIEWS_LIST_PATH)
  @Logging(
      item = "Dashboard",
      action = "getContentViewsList",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<PageResponse<ContentTotalViewStatsResponse>>>
      getContentViewsList(
          @Auth Accessor accessor,
          @RequestParam(value = "period") String period,
          @RequestParam(defaultValue = "0") int page) {
    // Period별 페이지 사이즈 동적 결정
    int expectedDays =
        switch (period) {
          case "TODAY" -> 1;
          case "LAST_7_DAYS" -> 7;
          case "LAST_30_DAYS" -> 30;
          case "THIS_MONTH" -> LocalDate.now().getDayOfMonth();
          case "LAST_MONTH" -> YearMonth.now().minusMonths(1).lengthOfMonth();
          default -> throw new IllegalArgumentException("Invalid period: " + period);
        };

    int pageSize = Math.min(expectedDays, 20); // 최대 20개
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "statDate"));

    PageResponse<ContentTotalViewStatsDTO> dtoPage =
        dashboardService.getContentTotalViewStats(accessor.getUserId(), period, pageable);

    PageResponse<ContentTotalViewStatsResponse> responsePage =
        dashboardMapper.toContentTotalViewStatsResponsePage(dtoPage);

    return responseHelper.success(
        responsePage, DASHBOARD_CONTENTS_VIEW_STATS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // TODO(4): 오늘/지난 7일/최근 30일/이번 달/지난 달 선택에 따른 마켓 상세 조회수 제공 + 유입 경로 제공
  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[📊 대시보드 - 마켓 날짜별 조회수 조회] 마켓 조회수 조회",
      description = "조회 날짜, 조회 일, 조회수를 반환합니다.")
  @GetMapping(DASHBOARD_MARKET_VIEW_STATS_PATH)
  @Logging(
      item = "Dashboard",
      action = "getMarketViewStats",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<PageResponse<MarketViewStatsResponse>>> getMarketViewStats(
      @Auth Accessor accessor,
      @PathVariable("marketId") Long marketId,
      @RequestParam(value = "period") String period,
      @RequestParam(defaultValue = "0") int page) {

    // Period별 페이지 사이즈 동적 결정
    int expectedDays =
        switch (period) {
          case "TODAY" -> 1;
          case "LAST_7_DAYS" -> 7;
          case "LAST_30_DAYS" -> 30;
          case "THIS_MONTH" -> LocalDate.now().getDayOfMonth();
          case "LAST_MONTH" -> YearMonth.now().minusMonths(1).lengthOfMonth();
          default -> throw new IllegalArgumentException("Invalid period: " + period);
        };

    int pageSize = Math.min(expectedDays, 20); // 최대 20개
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "statDate"));

    PageResponse<MarketViewStatsDTO> dtoPage =
        dashboardService.getMarketViewStats(accessor.getUserId(), marketId, period, pageable);

    PageResponse<MarketViewStatsResponse> responsePage =
        dashboardMapper.toMarketViewStatsResponsePage(dtoPage);

    return responseHelper.success(
        responsePage, DASHBOARD_MARKET_DETAIL_STATS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // TODO(5): 오늘/지난 7일/최근 30일/이번 달/지난 달 선택에 따른 콘텐츠 상세 조회수 제공 + 유입 경로 제공

  @RequireRole("ROLE_SELLER")
  @Operation(
      summary = "[📊 대시보드 - 콘텐츠 날짜 유형별 조회수 조회] 콘텐츠 조회수 조회",
      description = "조회 날짜, 조회 일, 조회수를 반환합니다.")
  @GetMapping(DASHBOARD_CONTENT_VIEW_STATS_PATH)
  @Logging(
      item = "Dashboard",
      action = "getContentViewStats",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<PageResponse<ContentViewStatsResponse>>> getContentViewStats(
      @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @RequestParam(value = "period") String period,
      @RequestParam(defaultValue = "0") int page) {

    // Period별 페이지 사이즈 동적 결정
    int expectedDays =
        switch (period) {
          case "TODAY" -> 1;
          case "LAST_7_DAYS" -> 7;
          case "LAST_30_DAYS" -> 30;
          case "THIS_MONTH" -> LocalDate.now().getDayOfMonth();
          case "LAST_MONTH" -> YearMonth.now().minusMonths(1).lengthOfMonth();
          default -> throw new IllegalArgumentException("Invalid period: " + period);
        };

    int pageSize = Math.min(expectedDays, 20); // 최대 20개
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "statDate"));

    PageResponse<ContentViewStatsDTO> dtoPage =
        dashboardService.getContentViewStats(accessor.getUserId(), contentId, period, pageable);

    PageResponse<ContentViewStatsResponse> responsePage =
        dashboardMapper.toContentViewStatsResponsePage(dtoPage);

    return responseHelper.success(
        responsePage, DASHBOARD_CONTENT_VIEW_STATS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @RequireRole("ROLE_SELLER")
  @GetMapping(DASHBOARD_CONTENT_REFERRER_STATS_PATH)
  public ResponseEntity<GrobleResponse<PageResponse<ReferrerStatsResponse>>>
      getContentReferrerStats(
          @Auth Accessor accessor,
          @PathVariable("contentId") Long contentId,
          @RequestParam(value = "period") String period,
          @RequestParam(value = "page", defaultValue = "0") int page) {
    Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "visitCount"));

    PageResponse<ReferrerStatsDTO> dtoPage =
        dashboardService.getContentReferrerStats(accessor.getUserId(), contentId, period, pageable);

    PageResponse<ReferrerStatsResponse> responsePage =
        dashboardMapper.toReferrerStatsResponsePage(dtoPage);

    return responseHelper.success(
        responsePage, DASHBOARD_CONTENT_REFERRER_STATS_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
