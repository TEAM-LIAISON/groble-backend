package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.request.AdminMakerVerifyRequest;
import liaison.groble.application.admin.service.AdminService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/")
@Tag(name = "관리자 기능 관련 API", description = "관리자 기능 API")
public class AdminController {

  private final AdminService adminService;

  @Operation(summary = "메이커 인증 요청 처리", description = "메이커 인증 요청을 처리합니다. [수락/거절]")
  @RequireRole("ROLE_ADMIN")
  @PostMapping("/maker/verify")
  public ResponseEntity<GrobleResponse<Void>> verifyMakerAccount(
      @Auth Accessor accessor, @Valid @RequestBody AdminMakerVerifyRequest request) {

    adminService.verifyMaker(
        accessor.getUserId(), request.getNickname(), request.getStatus().name());
    return ResponseEntity.ok(GrobleResponse.success(null));
  }
}
