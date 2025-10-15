package liaison.groble.api.server.admin;

import java.time.LocalDate;
import java.time.YearMonth;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.request.AdminMakerVerifyRequest;
import liaison.groble.api.model.admin.request.AdminMemoRequest;
import liaison.groble.api.model.admin.response.maker.AdminMakerDetailInfoResponse;
import liaison.groble.api.model.admin.response.maker.AdminMemoResponse;
import liaison.groble.api.model.dashboard.response.DashboardOverviewResponse;
import liaison.groble.api.model.dashboard.response.DashboardViewStatsResponse;
import liaison.groble.api.model.dashboard.response.MarketViewStatsResponse;
import liaison.groble.api.model.dashboard.response.ReferrerStatsResponse;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.api.server.admin.docs.AdminMakerSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.dto.AdminMakerDetailInfoDTO;
import liaison.groble.application.admin.dto.AdminMemoDTO;
import liaison.groble.application.admin.service.AdminMakerService;
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
import liaison.groble.mapping.admin.AdminMakerMapper;
import liaison.groble.mapping.dashboard.DashboardMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.Admin.MAKER_BASE)
@Tag(name = AdminMakerSwaggerDocs.TAG_NAME, description = AdminMakerSwaggerDocs.TAG_DESCRIPTION)
public class AdminMakerController extends BaseController {

  private final AdminMakerService adminMakerService;
  private final AdminMakerMapper adminMakerMapper;
  private final DashboardService dashboardService;
  private final DashboardMapper dashboardMapper;

  public AdminMakerController(
      ResponseHelper responseHelper,
      AdminMakerService adminMakerService,
      AdminMakerMapper adminMakerMapper,
      DashboardService dashboardService,
      DashboardMapper dashboardMapper) {
    super(responseHelper);
    this.adminMakerService = adminMakerService;
    this.adminMakerMapper = adminMakerMapper;
    this.dashboardService = dashboardService;
    this.dashboardMapper = dashboardMapper;
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.GET_MAKER_DETAIL_SUMMARY,
      description = AdminMakerSwaggerDocs.GET_MAKER_DETAIL_DESCRIPTION)
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = MakerIntroSectionResponse.class)))
  @Logging(
      item = "AdminMaker",
      action = "getMakerDetailInfo",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.MAKER_DETAIL)
  public ResponseEntity<GrobleResponse<AdminMakerDetailInfoResponse>> getMakerDetailInfo(
      @Auth Accessor accessor, @Valid @PathVariable("nickname") String nickname) {

    AdminMakerDetailInfoDTO adminMakerServiceMakerDetailInfo =
        adminMakerService.getMakerDetailInfo(accessor.getUserId(), nickname);
    AdminMakerDetailInfoResponse response =
        adminMakerMapper.toAdminMakerDetailInfoResponse(adminMakerServiceMakerDetailInfo);

    return success(response, ResponseMessages.Admin.MAKER_DETAIL_INFO_RETRIEVED);
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.VERIFY_MAKER_SUMMARY,
      description = AdminMakerSwaggerDocs.VERIFY_MAKER_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.MAKER_VERIFY)
  public ResponseEntity<GrobleResponse<Void>> verifyMakerAccount(
      @Auth Accessor accessor, @Valid @RequestBody AdminMakerVerifyRequest request) {

    return switch (request.getStatus()) {
      case APPROVED -> {
        adminMakerService.approveMaker(request.getNickname());
        yield successVoid(ResponseMessages.Admin.MAKER_VERIFY_APPROVED);
      }
      case REJECTED -> {
        adminMakerService.rejectMaker(request.getNickname(), request.getRejectionReason());
        yield successVoid(ResponseMessages.Admin.MAKER_VERIFY_REJECTED);
      }
    };
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.SAVE_ADMIN_MEMO_SUMMARY,
      description = AdminMakerSwaggerDocs.SAVE_ADMIN_MEMO_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.MAKER_MEMO)
  public ResponseEntity<GrobleResponse<AdminMemoResponse>> saveAdminMemo(
      @Auth Accessor accessor,
      @Valid @PathVariable("nickname") String nickname,
      @Valid @RequestBody AdminMemoRequest adminMemoRequest) {
    AdminMemoDTO memoDTO = adminMakerMapper.toAdminMemoDTO(adminMemoRequest);

    AdminMemoDTO savedAdminMemoDTO =
        adminMakerService.saveAdminMemo(accessor.getUserId(), nickname, memoDTO);
    AdminMemoResponse savedAdminMemo = adminMakerMapper.toAdminMemoResponse(savedAdminMemoDTO);
    return success(savedAdminMemo, ResponseMessages.Admin.MAKER_MEMO_SAVED);
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.MAKER_DASHBOARD_OVERVIEW_SUMMARY,
      description = AdminMakerSwaggerDocs.MAKER_DASHBOARD_OVERVIEW_DESCRIPTION)
  @Logging(
      item = "AdminMaker",
      action = "getMakerDashboardOverview",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.MAKER_DASHBOARD_OVERVIEW)
  public ResponseEntity<GrobleResponse<DashboardOverviewResponse>> getMakerDashboardOverview(
      @Auth Accessor accessor, @PathVariable("marketLinkUrl") String marketLinkUrl) {
    DashboardOverviewDTO dashboardOverviewDTO =
        dashboardService.getDashboardOverviewByMarketLink(marketLinkUrl);
    DashboardOverviewResponse response =
        dashboardMapper.toDashboardOverviewResponse(dashboardOverviewDTO);
    return success(response, ResponseMessages.Admin.MAKER_DASHBOARD_OVERVIEW_RETRIEVED);
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.MAKER_DASHBOARD_VIEW_STATS_SUMMARY,
      description = AdminMakerSwaggerDocs.MAKER_DASHBOARD_VIEW_STATS_DESCRIPTION)
  @Logging(
      item = "AdminMaker",
      action = "getMakerDashboardViewStats",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.MAKER_DASHBOARD_VIEW_STATS)
  public ResponseEntity<GrobleResponse<DashboardViewStatsResponse>> getMakerDashboardViewStats(
      @Auth Accessor accessor,
      @PathVariable("marketLinkUrl") String marketLinkUrl,
      @RequestParam("period") String period) {
    DashboardViewStatsDTO viewStatsDTO =
        dashboardService.getViewStatsByMarketLink(marketLinkUrl, period);
    DashboardViewStatsResponse response =
        dashboardMapper.toDashboardViewStatsResponse(viewStatsDTO);
    return success(response, ResponseMessages.Admin.MAKER_DASHBOARD_VIEW_STATS_RETRIEVED);
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.MAKER_DASHBOARD_MARKET_VIEW_STATS_SUMMARY,
      description = AdminMakerSwaggerDocs.MAKER_DASHBOARD_MARKET_VIEW_STATS_DESCRIPTION)
  @Logging(
      item = "AdminMaker",
      action = "getMakerDashboardMarketViewStats",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.MAKER_DASHBOARD_MARKET_VIEW_STATS)
  public ResponseEntity<GrobleResponse<PageResponse<MarketViewStatsResponse>>>
      getMakerDashboardMarketViewStats(
          @Auth Accessor accessor,
          @PathVariable("marketLinkUrl") String marketLinkUrl,
          @RequestParam("period") String period,
          @RequestParam(value = "page", defaultValue = "0") int page) {

    int expectedDays =
        switch (period) {
          case "TODAY" -> 1;
          case "LAST_7_DAYS" -> 7;
          case "LAST_30_DAYS" -> 30;
          case "THIS_MONTH" -> LocalDate.now().getDayOfMonth();
          case "LAST_MONTH" -> YearMonth.now().minusMonths(1).lengthOfMonth();
          default -> throw new IllegalArgumentException("Invalid period: " + period);
        };

    int pageSize = Math.min(expectedDays, 20);
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "statDate"));

    PageResponse<MarketViewStatsDTO> dtoPage =
        dashboardService.getMarketViewStatsByMarketLink(marketLinkUrl, period, pageable);
    PageResponse<MarketViewStatsResponse> responsePage =
        dashboardMapper.toMarketViewStatsResponsePage(dtoPage);
    return success(
        responsePage, ResponseMessages.Admin.MAKER_DASHBOARD_MARKET_VIEW_STATS_RETRIEVED);
  }

  @Operation(
      summary = AdminMakerSwaggerDocs.MAKER_DASHBOARD_MARKET_REFERRER_STATS_SUMMARY,
      description = AdminMakerSwaggerDocs.MAKER_DASHBOARD_MARKET_REFERRER_STATS_DESCRIPTION)
  @Logging(
      item = "AdminMaker",
      action = "getMakerDashboardMarketReferrerStats",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.MAKER_DASHBOARD_MARKET_REFERRER_STATS)
  public ResponseEntity<GrobleResponse<PageResponse<ReferrerStatsResponse>>>
      getMakerDashboardMarketReferrerStats(
          @Auth Accessor accessor,
          @PathVariable("marketLinkUrl") String marketLinkUrl,
          @RequestParam("period") String period,
          @RequestParam(value = "page", defaultValue = "0") int page) {

    Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "visitCount"));
    PageResponse<ReferrerStatsDTO> dtoPage =
        dashboardService.getMarketReferrerStatsByMarketLink(marketLinkUrl, period, pageable);
    PageResponse<ReferrerStatsResponse> responsePage =
        dashboardMapper.toReferrerStatsResponsePage(dtoPage);
    return success(
        responsePage, ResponseMessages.Admin.MAKER_DASHBOARD_MARKET_REFERRER_STATS_RETRIEVED);
  }
}
