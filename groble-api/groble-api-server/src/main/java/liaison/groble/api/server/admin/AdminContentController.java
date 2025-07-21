package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "[✅ 관리자] 관리자의 콘텐츠 목록 조회 및 모니터링 API")
public class AdminContentController {

  // API 경로 상수화
  private static final String ADMIN_CONTENT_SUMMARY_INFO_PATH = "/contents";
  private static final String ADMIN_CONTENT_EXAMINE_PATH = "/content/{contentId}/examine";

  // 응답 메시지 상수화
  private static final String ADMIN_CONTENT_SUMMARY_INFO_SUCCESS_MESSAGE = "관리자 콘텐츠 목록 조회에 성공했습니다.";
  private static final String ADMIN_CONTENT_EXAMINE_SUCCESS_MESSAGE = "콘텐츠 심사 승인에 성공했습니다.";
  private static final String ADMIN_CONTENT_REJECT_SUCCESS_MESSAGE = "콘텐츠 심사 반려에 성공했습니다.";

  // Service
  private final AdminContentService adminContentService;

  // Mapper
  private final AdminContentMapper adminContentMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @AdminContentSummaryInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ADMIN_CONTENT_SUMMARY_INFO_PATH)
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

    return responseHelper.success(
        responsePage, ADMIN_CONTENT_SUMMARY_INFO_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @ContentExamine
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ADMIN_CONTENT_EXAMINE_PATH)
  public ResponseEntity<GrobleResponse<Void>> examineContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @Valid @RequestBody ContentExamineRequest examineRequest) {

    return switch (examineRequest.getAction()) {
      case APPROVE -> {
        adminContentService.approveContent(contentId);
        yield responseHelper.success(null, ADMIN_CONTENT_EXAMINE_SUCCESS_MESSAGE, HttpStatus.OK);
      }
      case REJECT -> {
        adminContentService.rejectContent(contentId, examineRequest.getRejectReason());
        yield responseHelper.success(null, ADMIN_CONTENT_REJECT_SUCCESS_MESSAGE, HttpStatus.OK);
      }
    };
  }
}
