package liaison.groble.api.server.admin;

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

import liaison.groble.api.model.admin.response.AdminContentSummaryInfoResponse;
import liaison.groble.api.model.admin.response.swagger.AdminContentSummaryInfo;
import liaison.groble.api.model.content.request.examine.ContentExamineRequest;
import liaison.groble.api.model.content.response.swagger.ContentExamine;
import liaison.groble.api.model.dashboard.response.ContentOverviewResponse;
import liaison.groble.api.model.dashboard.response.ContentTotalViewStatsResponse;
import liaison.groble.api.server.admin.docs.AdminContentSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.dto.AdminContentSummaryInfoDTO;
import liaison.groble.application.admin.service.AdminContentService;
import liaison.groble.application.dashboard.dto.ContentTotalViewStatsDTO;
import liaison.groble.application.dashboard.dto.DashboardContentOverviewDTO;
import liaison.groble.application.dashboard.service.DashboardService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.admin.AdminContentMapper;
import liaison.groble.mapping.dashboard.DashboardMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.Admin.BASE)
@Tag(name = AdminContentSwaggerDocs.TAG_NAME, description = AdminContentSwaggerDocs.TAG_DESCRIPTION)
public class AdminContentController extends BaseController {

  private final AdminContentService adminContentService;
  private final AdminContentMapper adminContentMapper;
  private final DashboardService dashboardService;
  private final DashboardMapper dashboardMapper;

  public AdminContentController(
      ResponseHelper responseHelper,
      AdminContentService adminContentService,
      AdminContentMapper adminContentMapper,
      DashboardService dashboardService,
      DashboardMapper dashboardMapper) {
    super(responseHelper);
    this.adminContentService = adminContentService;
    this.adminContentMapper = adminContentMapper;
    this.dashboardService = dashboardService;
    this.dashboardMapper = dashboardMapper;
  }

  @Operation(
      summary = AdminContentSwaggerDocs.GET_ALL_CONTENTS_SUMMARY,
      description = AdminContentSwaggerDocs.GET_ALL_CONTENTS_DESCRIPTION)
  @AdminContentSummaryInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.CONTENTS)
  public ResponseEntity<GrobleResponse<PageResponse<AdminContentSummaryInfoResponse>>>
      getAllContents(
          @Auth Accessor accessor,
          @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
              @RequestParam(value = "page", defaultValue = "0")
              int page,
          @Parameter(description = "페이지당 주문 수", example = "12")
              @RequestParam(value = "size", defaultValue = "12")
              int size,
          @Parameter(description = "정렬 기준 (property,direction)", example = "createdAt,desc")
              @RequestParam(value = "sort", defaultValue = "createdAt")
              String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<AdminContentSummaryInfoDTO> infoDTOPage =
        adminContentService.getAllContents(pageable);
    PageResponse<AdminContentSummaryInfoResponse> responsePage =
        adminContentMapper.toAdminContentSummaryInfoResponsePage(infoDTOPage);

    return success(responsePage, ResponseMessages.Admin.CONTENT_SUMMARY_INFO_RETRIEVED);
  }

  @Operation(
      summary = AdminContentSwaggerDocs.MAKER_DASHBOARD_CONTENTS_SUMMARY,
      description = AdminContentSwaggerDocs.MAKER_DASHBOARD_CONTENTS_DESCRIPTION)
  @Logging(
      item = "AdminContent",
      action = "getMakerDashboardContents",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.MAKER_PATH_PREFIX + ApiPaths.Admin.MAKER_DASHBOARD_CONTENTS)
  public ResponseEntity<GrobleResponse<PageResponse<ContentOverviewResponse>>>
      getMakerDashboardContents(
          @Auth Accessor accessor,
          @PathVariable("marketLinkUrl") String marketLinkUrl,
          @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
              @RequestParam(value = "page", defaultValue = "0")
              int page,
          @Parameter(description = "페이지당 콘텐츠 수", example = "20")
              @RequestParam(value = "size", defaultValue = "20")
              int size,
          @Parameter(description = "정렬 기준 (property,direction)", example = "createdAt,desc")
              @RequestParam(value = "sort", defaultValue = "createdAt")
              String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<DashboardContentOverviewDTO> dtoPage =
        dashboardService.getMyContentsListByMarketLink(marketLinkUrl, pageable);
    PageResponse<ContentOverviewResponse> responsePage =
        dashboardMapper.toContentOverviewResponsePage(dtoPage);

    return success(responsePage, ResponseMessages.Admin.MAKER_DASHBOARD_CONTENTS_RETRIEVED);
  }

  @Operation(
      summary = AdminContentSwaggerDocs.MAKER_DASHBOARD_CONTENT_VIEW_STATS_SUMMARY,
      description = AdminContentSwaggerDocs.MAKER_DASHBOARD_CONTENT_VIEW_STATS_DESCRIPTION)
  @Logging(
      item = "AdminContent",
      action = "getMakerDashboardContentViewStats",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.MAKER_PATH_PREFIX + ApiPaths.Admin.MAKER_DASHBOARD_CONTENT_VIEW_STATS)
  public ResponseEntity<GrobleResponse<PageResponse<ContentTotalViewStatsResponse>>>
      getMakerDashboardContentViewStats(
          @Auth Accessor accessor,
          @PathVariable("marketLinkUrl") String marketLinkUrl,
          @RequestParam("period") String period,
          @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
              @RequestParam(value = "page", defaultValue = "0")
              int page) {

    Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "statDate"));
    PageResponse<ContentTotalViewStatsDTO> dtoPage =
        dashboardService.getContentTotalViewStatsByMarketLink(marketLinkUrl, period, pageable);
    PageResponse<ContentTotalViewStatsResponse> responsePage =
        dashboardMapper.toContentTotalViewStatsResponsePage(dtoPage);

    return success(
        responsePage, ResponseMessages.Admin.MAKER_DASHBOARD_CONTENT_VIEW_STATS_RETRIEVED);
  }

  @Operation(
      summary = AdminContentSwaggerDocs.EXAMINE_CONTENT_SUMMARY,
      description = AdminContentSwaggerDocs.EXAMINE_CONTENT_DESCRIPTION)
  @ContentExamine
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.CONTENT_EXAMINE)
  public ResponseEntity<GrobleResponse<Void>> examineContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @Valid @RequestBody ContentExamineRequest examineRequest) {

    return switch (examineRequest.getAction()) {
      case APPROVE -> {
        adminContentService.approveContent(contentId);
        yield successVoid(ResponseMessages.Admin.CONTENT_EXAMINE_APPROVED);
      }
      case REJECT -> {
        adminContentService.rejectContent(contentId, examineRequest.getRejectReason());
        yield successVoid(ResponseMessages.Admin.CONTENT_EXAMINE_REJECTED);
      }
    };
  }
}
