package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
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
import liaison.groble.api.server.admin.docs.AdminContentSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.dto.AdminContentSummaryInfoDTO;
import liaison.groble.application.admin.service.AdminContentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.admin.AdminContentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.Admin.BASE)
@Tag(name = AdminContentSwaggerDocs.TAG_NAME, description = AdminContentSwaggerDocs.TAG_DESCRIPTION)
public class AdminContentController extends BaseController {

  private final AdminContentService adminContentService;
  private final AdminContentMapper adminContentMapper;

  public AdminContentController(
      ResponseHelper responseHelper,
      AdminContentService adminContentService,
      AdminContentMapper adminContentMapper) {
    super(responseHelper);
    this.adminContentService = adminContentService;
    this.adminContentMapper = adminContentMapper;
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
