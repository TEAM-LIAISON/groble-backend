package liaison.groble.api.server.verification;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.auth.request.PhoneNumberVerifyCodeRequest;
import liaison.groble.api.model.auth.request.PhoneNumberVerifyRequest;
import liaison.groble.application.auth.service.PhoneAuthService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/verification")
@Tag(
    name = "[⭐ 번호 인증] 전화번호를 활용한 코드 전송, 코드 검증 등 API",
    description = "회원가입 및 전화번호 변경 과정에서 전화번호를 검증하는 기능을 제공합니다.")
public class PhoneVerificationController {

  // API 경로 상수화
  private static final String PHONE_VERIFY_REQUEST_PATH = "/phone-number/verify-request";
  private static final String PHONE_VERIFY_CODE_PATH = "/phone-number/verify-code";

  // 응답 메시지 상수화
  private static final String PHONE_NUMBER_VERIFY_REQUEST_SUCCESS_MESSAGE =
      "전화번호 인증 요청이 성공적으로 완료되었습니다.";
  private static final String PHONE_NUMBER_VERIFY_CODE_SUCCESS_MESSAGE =
      "전화번호 인증 코드 검증이 성공적으로 완료되었습니다.";

  private final PhoneAuthService phoneAuthService;
  private final ResponseHelper responseHelper;

  @Operation(summary = "[✅ 번호 인증 요청] 회원가입 및 전화번호 변경에 사용", description = "전화번호 인증 코드를 발송합니다.")
  @PostMapping(PHONE_VERIFY_REQUEST_PATH)
  public ResponseEntity<GrobleResponse<Void>> authPhoneNumber(
      @Auth Accessor accessor,
      @Parameter(description = "전화번호 인증 정보", required = true) @Valid @RequestBody
          PhoneNumberVerifyRequest request) {

    phoneAuthService.sendVerificationCodeForUser(accessor.getId(), request.getPhoneNumber());

    return responseHelper.success(null, PHONE_NUMBER_VERIFY_REQUEST_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(summary = "[✅ 번호 검증 요청] 회원가입 및 전화번호 변경에 사용", description = "전화번호로 발송된 인증 코드를 검증합니다.")
  @PostMapping(PHONE_VERIFY_CODE_PATH)
  public ResponseEntity<GrobleResponse<Void>> verifyPhoneNumber(
      @Auth Accessor accessor,
      @Parameter(description = "전화번호 인증 정보", required = true) @Valid @RequestBody
          PhoneNumberVerifyCodeRequest request) {

    phoneAuthService.verifyCodeForUser(
        accessor.getId(), request.getPhoneNumber(), request.getVerificationCode());

    return responseHelper.success(null, PHONE_NUMBER_VERIFY_CODE_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
