package liaison.groble.api.server.terms;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.terms.response.SettingResponse;
import liaison.groble.api.model.user.request.AdvertisingAgreementRequest;
import liaison.groble.application.terms.service.TermsService;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 약관 관련 API 컨트롤러 */
@Slf4j
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
@Tag(
    name = "[👨🏻‍⚖️ 약관] 광고성 정보 수신 동의 상태 변경 및 조회 API",
    description = "광고성 정보 수신 동의 상태를 변경하고 조회하는 API입니다.")
public class TermsController {

  // API 경로 상수화
  private static final String ADVERTISING_AGREEMENT_PATH = "/users/me/advertising-agreement";

  // 응답 메시지 상수화
  private static final String SETTING_PAGE_SUCCESS_MESSAGE =
      "설정 탭에서 광고성 정보 수신 동의 여부 및 탈퇴 가능 여부 조회 성공";
  private static final String ADVERTISING_AGREEMENT_SUCCESS_MESSAGE = "광고성 정보 수신 동의 상태 변경 성공";
  // Service
  private final UserService userService;
  private final TermsService termsService;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[✅ 설정 탭] 광고성 정보 수신 동의 여부 및 탈퇴 가능 여부 조회",
      description = "현재 로그인한 사용자의 광고성 정보 수신 동의 여부와 탈퇴 가능 여부를 조회합니다.")
  @GetMapping(ADVERTISING_AGREEMENT_PATH)
  public ResponseEntity<GrobleResponse<SettingResponse>> getAdvertisingAgreementStatus(
      @Auth Accessor accessor) {
    boolean isAdvertisingAgreed = termsService.getAdvertisingAgreementStatus(accessor.getUserId());
    boolean isAllowWithdraw = userService.isAllowWithdraw(accessor.getUserId());

    SettingResponse settingResponse =
        SettingResponse.builder()
            .isAdvertisingAgreement(isAdvertisingAgreed)
            .isAllowWithdraw(isAllowWithdraw)
            .build();

    return responseHelper.success(settingResponse, SETTING_PAGE_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(summary = "[✅ 광고성 정보 수신 동의 변경]", description = "현재 로그인한 사용자의 광고성 정보 수신 동의 여부를 변경합니다.")
  @PostMapping(ADVERTISING_AGREEMENT_PATH)
  public ResponseEntity<GrobleResponse<Void>> updateAdvertisingAgreementStatus(
      @Auth Accessor accessor,
      @Valid @RequestBody AdvertisingAgreementRequest request,
      HttpServletRequest httpRequest) {

    // 1. 서비스 호출
    termsService.updateAdvertisingAgreementStatus(
        accessor.getUserId(),
        request.getAgreed(),
        httpRequest.getRemoteAddr(),
        httpRequest.getHeader("User-Agent"));

    return responseHelper.success(null, ADVERTISING_AGREEMENT_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
