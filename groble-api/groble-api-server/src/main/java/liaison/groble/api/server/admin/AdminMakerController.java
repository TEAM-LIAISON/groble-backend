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
import liaison.groble.application.admin.dto.AdminMakerDetailInfoDto;
import liaison.groble.application.admin.service.AdminMakerService;
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
@RequestMapping("/api/v1/admin")
@Tag(name = "관리자 메이커 기능 관련 API", description = "관리자 메이커 인증 API")
public class AdminMakerController {

  private final AdminMakerService adminMakerService;

  @AdminMakerDetailInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping("/maker/{nickname}")
  public ResponseEntity<GrobleResponse<AdminMakerDetailInfoResponse>> getMakerDetailInfo(
      @Auth Accessor accessor, @Valid @PathVariable("nickname") String nickname) {

    AdminMakerDetailInfoDto infoDto =
        adminMakerService.getMakerDetailInfo(accessor.getUserId(), nickname);
    AdminMakerDetailInfoResponse response = toAdminMakerDetailInfoResponseFromDto(infoDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "메이커 상세 정보 조회 성공"));
  }

  @Operation(summary = "메이커 인증 요청 처리", description = "메이커 인증 요청을 처리합니다. [수락/거절]")
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

  private AdminMakerDetailInfoResponse toAdminMakerDetailInfoResponseFromDto(
      AdminMakerDetailInfoDto infoDto) {
    return AdminMakerDetailInfoResponse.builder()
        .isBusinessMaker(infoDto.isBusinessMaker())
        .bankAccountOwner(infoDto.getBankAccountOwner())
        .bankName(infoDto.getBankName())
        .bankAccountNumber(infoDto.getBankAccountNumber())
        .copyOfBankbookUrl(infoDto.getCopyOfBankbookUrl())
        .businessType(infoDto.getBusinessType())
        .businessCategory(infoDto.getBusinessCategory())
        .businessSector(infoDto.getBusinessSector())
        .businessName(infoDto.getBusinessName())
        .representativeName(infoDto.getRepresentativeName())
        .businessAddress(infoDto.getBusinessAddress())
        .businessLicenseFileUrl(infoDto.getBusinessLicenseFileUrl())
        .taxInvoiceEmail(infoDto.getTaxInvoiceEmail())
        .build();
  }
}
