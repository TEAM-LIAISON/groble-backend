package liaison.groble.api.server.home;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.hometest.phoneauth.request.HomeTestCompleteRequest;
import liaison.groble.api.model.hometest.phoneauth.request.HomeTestPhoneAuthCodeRequest;
import liaison.groble.api.model.hometest.phoneauth.request.HomeTestVerifyPhoneAuthRequest;
import liaison.groble.api.model.hometest.phoneauth.response.HomeTestCompleteResponse;
import liaison.groble.api.model.hometest.phoneauth.response.HomeTestPhoneAuthCodeResponse;
import liaison.groble.api.model.hometest.phoneauth.response.HomeTestVerifyPhoneAuthResponse;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.home.docs.HomeTestSwaggerDocs;
import liaison.groble.application.hometest.dto.HomeTestCompleteDTO;
import liaison.groble.application.hometest.dto.HomeTestPhoneAuthDTO;
import liaison.groble.application.hometest.dto.HomeTestVerificationResultDTO;
import liaison.groble.application.hometest.dto.HomeTestVerifyAuthDTO;
import liaison.groble.application.hometest.service.HomeTestPhoneAuthService;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.hometest.HomeTestAuthMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.HomeTest.BASE)
@Tag(name = HomeTestSwaggerDocs.TAG_NAME, description = HomeTestSwaggerDocs.TAG_DESCRIPTION)
public class HomeTestPhoneAuthController extends BaseController {

  private final HomeTestAuthMapper homeTestAuthMapper;
  private final HomeTestPhoneAuthService homeTestPhoneAuthService;

  public HomeTestPhoneAuthController(
      ResponseHelper responseHelper,
      HomeTestAuthMapper homeTestAuthMapper,
      HomeTestPhoneAuthService homeTestPhoneAuthService) {
    super(responseHelper);
    this.homeTestAuthMapper = homeTestAuthMapper;
    this.homeTestPhoneAuthService = homeTestPhoneAuthService;
  }

  @Operation(
      summary = HomeTestSwaggerDocs.SEND_CODE_SUMMARY,
      description = HomeTestSwaggerDocs.SEND_CODE_DESCRIPTION)
  @PostMapping(ApiPaths.HomeTest.SEND_CODE)
  public ResponseEntity<GrobleResponse<HomeTestPhoneAuthCodeResponse>> sendPhoneAuthCode(
      @Valid @RequestBody HomeTestPhoneAuthCodeRequest request) {

    HomeTestPhoneAuthDTO requestDto = homeTestAuthMapper.toPhoneAuthDTO(request);
    HomeTestPhoneAuthDTO responseDto = homeTestPhoneAuthService.sendAuthCode(requestDto);
    HomeTestPhoneAuthCodeResponse response = homeTestAuthMapper.toPhoneAuthResponse(responseDto);

    return success(response, ResponseMessages.HomeTest.PHONE_AUTH_CODE_SENT);
  }

  @Operation(
      summary = HomeTestSwaggerDocs.VERIFY_CODE_SUMMARY,
      description = HomeTestSwaggerDocs.VERIFY_CODE_DESCRIPTION)
  @PostMapping(ApiPaths.HomeTest.VERIFY_CODE)
  public ResponseEntity<GrobleResponse<HomeTestVerifyPhoneAuthResponse>> verifyPhoneAuthCode(
      @Valid @RequestBody HomeTestVerifyPhoneAuthRequest request) {

    HomeTestVerifyAuthDTO verifyAuthDTO = homeTestAuthMapper.toVerifyAuthDTO(request);
    HomeTestVerificationResultDTO verificationResult =
        homeTestPhoneAuthService.verifyAuthCode(verifyAuthDTO);
    HomeTestVerifyPhoneAuthResponse response =
        homeTestAuthMapper.toVerifyResponse(verificationResult);

    return success(response, ResponseMessages.HomeTest.PHONE_AUTH_VERIFIED);
  }

  @Operation(
      summary = HomeTestSwaggerDocs.COMPLETE_FLOW_SUMMARY,
      description = HomeTestSwaggerDocs.COMPLETE_FLOW_DESCRIPTION)
  @PostMapping(ApiPaths.HomeTest.COMPLETE)
  public ResponseEntity<GrobleResponse<HomeTestCompleteResponse>> completeTestFlow(
      @Valid @RequestBody HomeTestCompleteRequest request) {

    HomeTestCompleteDTO completeDTO = homeTestAuthMapper.toCompleteDTO(request);
    HomeTestVerificationResultDTO resultDTO =
        homeTestPhoneAuthService.completeTestFlow(completeDTO);
    HomeTestCompleteResponse response = homeTestAuthMapper.toCompleteResponse(resultDTO);

    return success(response, ResponseMessages.HomeTest.PHONE_AUTH_COMPLETED);
  }
}
