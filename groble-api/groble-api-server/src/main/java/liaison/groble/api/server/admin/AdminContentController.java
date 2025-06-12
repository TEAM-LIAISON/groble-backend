package liaison.groble.api.server.admin;

import java.util.List;
import java.util.stream.Collectors;

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
import liaison.groble.application.admin.dto.AdminContentSummaryInfoDto;
import liaison.groble.application.admin.service.AdminContentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "관리자의 콘텐츠 기능 관련 API", description = "관리자 콘텐츠 기능 API")
public class AdminContentController {

  private final AdminContentService adminContentService;

  @AdminContentSummaryInfo
  @RequireRole
  @GetMapping("/contents")
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
    Pageable pageable = createPageable(page, size, sort);
    PageResponse<AdminContentSummaryInfoDto> infoDtoPage =
        adminContentService.getAllContents(pageable);
    PageResponse<AdminContentSummaryInfoResponse> responsePage =
        toAdminOrderSummaryInfoResponsePage(infoDtoPage);

    return ResponseEntity.ok(GrobleResponse.success(responsePage));
  }

  @ContentExamine
  @RequireRole("ROLE_ADMIN")
  @PostMapping("/content/{contentId}/examine")
  public ResponseEntity<GrobleResponse<Void>> examineContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @Valid @RequestBody ContentExamineRequest examineRequest) {

    return switch (examineRequest.getAction()) {
      case APPROVE -> {
        adminContentService.approveContent(contentId);
        yield ResponseEntity.ok(GrobleResponse.success(null, "콘텐츠 심사 승인 성공"));
      }
      case REJECT -> {
        adminContentService.rejectContent(contentId, examineRequest.getRejectReason());
        yield ResponseEntity.ok(GrobleResponse.success(null, "콘텐츠 심사 반려 성공"));
      }
    };
  }

  private Pageable createPageable(int page, int size, String sort) {
    // sort 파라미터가 없거나 빈 문자열인 경우 기본값 설정
    if (sort == null || sort.isBlank()) {
      sort = "createdAt";
    }

    // "property,direction" 형태로 분리
    String[] parts = sort.split(",");
    String property = parts[0].trim();
    Sort.Direction direction = Sort.Direction.DESC; // 기본 방향

    // direction 지정이 있으면 파싱 시도
    if (parts.length > 1 && !parts[1].isBlank()) {
      try {
        direction = Sort.Direction.fromString(parts[1].trim());
      } catch (IllegalArgumentException e) {
        log.warn("잘못된 정렬 방향: {}. DESC로 설정합니다.", parts[1].trim());
      }
    }

    return PageRequest.of(page, size, Sort.by(direction, property));
  }

  private PageResponse<AdminContentSummaryInfoResponse> toAdminOrderSummaryInfoResponsePage(
      PageResponse<AdminContentSummaryInfoDto> dtoPage) {
    List<AdminContentSummaryInfoResponse> items =
        dtoPage.getItems().stream()
            .map(this::toAdminContentSummaryInfoResponseFromDto)
            .collect(Collectors.toList());

    return PageResponse.<AdminContentSummaryInfoResponse>builder()
        .items(items)
        .pageInfo(dtoPage.getPageInfo())
        .meta(dtoPage.getMeta())
        .build();
  }

  private AdminContentSummaryInfoResponse toAdminContentSummaryInfoResponseFromDto(
      AdminContentSummaryInfoDto infoDto) {
    return AdminContentSummaryInfoResponse.builder()
        .createdAt(infoDto.getCreatedAt())
        .contentType(infoDto.getContentType())
        .sellerName(infoDto.getSellerName())
        .contentTitle(infoDto.getContentTitle())
        .priceOptionLength(infoDto.getPriceOptionLength())
        .contentStatus(infoDto.getContentStatus())
        .adminContentCheckingStatus(infoDto.getAdminContentCheckingStatus())
        .build();
  }
}
