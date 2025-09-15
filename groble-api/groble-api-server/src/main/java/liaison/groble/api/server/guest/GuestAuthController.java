package liaison.groble.api.server.guest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.guest.request.GuestAuthCodeRequest;
import liaison.groble.api.model.guest.request.UpdateGuestUserInfoRequest;
import liaison.groble.api.model.guest.request.VerifyGuestAuthCodeRequest;
import liaison.groble.api.model.guest.response.GuestAuthCodeResponse;
import liaison.groble.api.model.guest.response.UpdateGuestUserInfoResponse;
import liaison.groble.api.model.guest.response.VerifyAuthCodeResponse;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.guest.docs.GuestAuthPostResponses;
import liaison.groble.api.server.guest.docs.GuestAuthSwaggerDocs;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.dto.GuestTokenDTO;
import liaison.groble.application.guest.dto.UpdateGuestUserInfoDTO;
import liaison.groble.application.guest.dto.UpdateGuestUserInfoResultDTO;
import liaison.groble.application.guest.dto.VerifyGuestAuthCodeDTO;
import liaison.groble.application.guest.service.GuestAuthService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.TokenCookieService;
import liaison.groble.mapping.guest.GuestAuthMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping(ApiPaths.Guest.BASE_AUTH)
@Tag(name = GuestAuthSwaggerDocs.TAG_NAME, description = GuestAuthSwaggerDocs.TAG_DESCRIPTION)
public class GuestAuthController extends BaseController {

  // Service
  private final GuestAuthService guestAuthService;
  private final TokenCookieService tokenCookieService;

  // Mapper
  private final GuestAuthMapper guestAuthMapper;

  public GuestAuthController(
      ResponseHelper responseHelper,
      GuestAuthMapper guestAuthMapper,
      GuestAuthService guestAuthService,
      TokenCookieService tokenCookieService) {
    super(responseHelper);
    this.guestAuthMapper = guestAuthMapper;
    this.guestAuthService = guestAuthService;
    this.tokenCookieService = tokenCookieService;
  }

  // === 비회원 전화번호 인증 요청 API ===
  @Operation(
      summary = GuestAuthSwaggerDocs.CODE_REQUEST_SUMMARY,
      description = GuestAuthSwaggerDocs.CODE_REQUEST_DESCRIPTION)
  @GuestAuthPostResponses.AuthCodeRequestResponses
  @Logging(
      item = "Guest",
      action = "sendGuestPhoneNumberAuthCode",
      includeParam = true,
      includeResult = true)
  @PostMapping(ApiPaths.Guest.PHONE_CODE_REQUEST)
  public ResponseEntity<GrobleResponse<GuestAuthCodeResponse>> sendGuestPhoneNumberAuthCode(
      @Valid @RequestBody GuestAuthCodeRequest guestAuthCodeRequest) {

    GuestAuthDTO requestAuthDTO = guestAuthMapper.toGuestAuthDTO(guestAuthCodeRequest);
    GuestAuthDTO responseAuthDTO = guestAuthService.sendGuestAuthCode(requestAuthDTO);

    GuestAuthCodeResponse guestAuthCodeResponse =
        guestAuthMapper.toGuestAuthCodeResponse(responseAuthDTO);

    return success(guestAuthCodeResponse, ResponseMessages.Guest.GUEST_AUTH_PHONE_REQUEST_SUCCESS);
  }

  // === 비회원 전화번호 인증 코드 검증 및 (임시/정식)토큰 발급 API ===
  @Operation(
      summary = GuestAuthSwaggerDocs.CODE_VERIFY_SUMMARY,
      description = GuestAuthSwaggerDocs.CODE_VERIFY_DESCRIPTION)
  @GuestAuthPostResponses.VerifyAuthCodeResponses
  @Logging(
      item = "Guest",
      action = "verifyGuestPhoneNumberAuthCode",
      includeParam = true,
      includeResult = true)
  @PostMapping(ApiPaths.Guest.PHONE_CODE_VERIFY)
  public ResponseEntity<GrobleResponse<VerifyAuthCodeResponse>> verifyGuestPhoneNumberAuthCode(
      @Valid @RequestBody VerifyGuestAuthCodeRequest verifyGuestAuthCodeRequest,
      HttpServletResponse response) {

    VerifyGuestAuthCodeDTO verifyGuestAuthCodeDTO =
        guestAuthMapper.toVerifyGuestAuthCodeDTO(verifyGuestAuthCodeRequest);
    GuestTokenDTO guestTokenDTO = guestAuthService.verifyGuestAuthCode(verifyGuestAuthCodeDTO);

    // 게스트 토큰 쿠키 설정
    tokenCookieService.addGuestTokenCookie(response, guestTokenDTO.getGuestToken());

    // DTO를 Response로 변환
    VerifyAuthCodeResponse verifyAuthCodeResponse =
        guestAuthMapper.toVerifyAuthCodeResponse(guestTokenDTO);

    return success(verifyAuthCodeResponse, "비회원 전화번호 인증 검증이 성공적으로 완료되었습니다.");
  }

  // === 비회원 사용자 정보 업데이트 및 정식 토큰 발급 API ===
  @Operation(
      summary = GuestAuthSwaggerDocs.UPDATE_GUEST_USER_INFO_SUMMARY,
      description = GuestAuthSwaggerDocs.UPDATE_GUEST_USER_INFO_DESCRIPTION)
  @GuestAuthPostResponses.UpdateGuestUserInfoResponses
  @Logging(
      item = "Guest",
      action = "updateGuestUserInfo",
      includeParam = true,
      includeResult = true)
  @PostMapping(ApiPaths.Guest.UPDATE_GUEST_USER_INFO)
  public ResponseEntity<GrobleResponse<UpdateGuestUserInfoResponse>> updateGuestUserInfo(
      @Valid @RequestBody UpdateGuestUserInfoRequest updateGuestUserInfoRequest,
      @Auth(required = false) Accessor accessor,
      HttpServletResponse response) {

    // DTO 변환
    UpdateGuestUserInfoDTO updateGuestUserInfoDTO =
        guestAuthMapper.toUpdateGuestUserInfoDTO(updateGuestUserInfoRequest);

    // 서비스 호출
    UpdateGuestUserInfoResultDTO resultDTO =
        guestAuthService.updateGuestUserInfo(accessor.getUserId(), updateGuestUserInfoDTO);

    // 새로운 토큰을 쿠키에 설정
    tokenCookieService.addGuestTokenCookie(response, resultDTO.getNewGuestToken());

    // Response 변환
    UpdateGuestUserInfoResponse updateResponse =
        guestAuthMapper.toUpdateGuestUserInfoResponse(resultDTO);

    return success(updateResponse, ResponseMessages.Guest.UPDATE_GUEST_USER_INFO_SUCCESS);
  }
}
