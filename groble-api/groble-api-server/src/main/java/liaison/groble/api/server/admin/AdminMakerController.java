package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.request.AdminMakerVerifyRequest;
import liaison.groble.api.model.admin.response.AdminMakerDetailInfoResponse;
import liaison.groble.api.model.admin.response.swagger.AdminMakerDetailInfo;
import liaison.groble.application.admin.dto.AdminMakerDetailInfoDTO;
import liaison.groble.application.admin.service.AdminMakerService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.mapping.admin.AdminMakerMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(
    name = "[✅ 관리자] 관리자 메이커 상세 조회 및 메이커 인증 API",
    description = "개인 메이커 및 사업자 메이커의 상세 정보를 조회하고, 메이커 인증 요청을 처리하는 API입니다.")
public class AdminMakerController {

  // API 경로 상수화
  private static final String MAKER_DETAIL_INFO_PATH = "/maker/{nickname}";
  // 응답 메시지 상수화

  // Service
  private final AdminMakerService adminMakerService;

  // Mapper
  private final AdminMakerMapper adminMakerMapper;

  @AdminMakerDetailInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping(MAKER_DETAIL_INFO_PATH)
  public ResponseEntity<GrobleResponse<AdminMakerDetailInfoResponse>> getMakerDetailInfo(
      @Auth Accessor accessor, @Valid @PathVariable("nickname") String nickname) {

    AdminMakerDetailInfoDTO adminMakerServiceMakerDetailInfo =
        adminMakerService.getMakerDetailInfo(accessor.getUserId(), nickname);
    AdminMakerDetailInfoResponse response =
        adminMakerMapper.toAdminMakerDetailInfoResponse(adminMakerServiceMakerDetailInfo);
    return ResponseEntity.ok(GrobleResponse.success(response, "메이커 상세 정보 조회 성공"));
  }

  @Operation(summary = "[✅ 관리자 메이커] 메이커 인증 요청 처리", description = "메이커 인증 요청을 처리합니다. [수락/거절]")
  @RequireRole("ROLE_ADMIN")
  @PostMapping("/maker/verify")
  public ResponseEntity<GrobleResponse<Void>> verifyMakerAccount(
      @Auth Accessor accessor, @Valid @RequestBody AdminMakerVerifyRequest request) {

    return switch (request.getStatus()) {
      case APPROVED -> {
        adminMakerService.approveMaker(accessor.getUserId(), request.getNickname());
        yield ResponseEntity.ok(GrobleResponse.success(null, "메이커 인증 승인 성공"));
      }
      case REJECTED -> {
        adminMakerService.rejectMaker(accessor.getUserId(), request.getNickname());
        yield ResponseEntity.ok(GrobleResponse.success(null, "메이커 인증 거절 성공"));
      }
    };
  }
}
