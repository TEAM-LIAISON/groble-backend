package liaison.groble.api.server.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.request.examine.ContentExamineRequest;
import liaison.groble.api.model.content.response.swagger.ContentExamine;
import liaison.groble.application.admin.service.AdminContentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

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

  @ContentExamine
  @RequireRole("ROLE_ADMIN")
  @PostMapping("/content/{contentId}/examine")
  public ResponseEntity<GrobleResponse<Void>> examineContent(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @PathVariable("contentId") Long contentId,
      @RequestBody ContentExamineRequest examineRequest) {
    final String APPROVE = "APPROVE";
    final String REJECT = "REJECT";

    String action = examineRequest.getAction();

    if (APPROVE.equals(action)) {
      adminContentService.approveContent(contentId);
      return ResponseEntity.ok(GrobleResponse.success(null, "콘텐츠 심사 승인 성공"));
    } else if (REJECT.equals(action)) {
      // 반려 사유가 있다면 함께 전달
      String rejectReason = examineRequest.getRejectReason();
      adminContentService.rejectContent(contentId, rejectReason);
      return ResponseEntity.ok(GrobleResponse.success(null, "콘텐츠 심사 반려 성공"));
    } else {
      throw new IllegalArgumentException("지원하지 않는 심사 액션입니다: " + action);
    }
  }
}
