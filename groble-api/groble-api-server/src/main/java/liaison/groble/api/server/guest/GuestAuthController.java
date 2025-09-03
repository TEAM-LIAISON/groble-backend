package liaison.groble.api.server.guest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.guest.request.GuestAuthCodeRequest;
import liaison.groble.api.model.guest.request.GuestAuthVerifyRequest;
import liaison.groble.api.model.guest.response.GuestAuthCodeResponse;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.guest.docs.GuestAuthPostResponses;
import liaison.groble.api.server.guest.docs.GuestAuthSwaggerDocs;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.dto.GuestAuthVerifyDTO;
import liaison.groble.application.guest.dto.GuestTokenDTO;
import liaison.groble.application.guest.service.GuestAuthService;
import liaison.groble.common.annotation.Logging;
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

  @Operation(
      summary = GuestAuthSwaggerDocs.CODE_VERIFY_SUMMARY,
      description = GuestAuthSwaggerDocs.CODE_VERIFY_DESCRIPTION)
  @Logging(
      item = "Guest",
      action = "verifyGuestPhoneNumberAuthCode",
      includeParam = true,
      includeResult = true)
  @PostMapping(ApiPaths.Guest.PHONE_CODE_VERIFY)
  public ResponseEntity<GrobleResponse<Void>> verifyGuestPhoneNumberAuthCode(
      @Valid @RequestBody GuestAuthVerifyRequest guestAuthVerifyRequest,
      HttpServletResponse response) {

    GuestAuthVerifyDTO guestAuthVerifyDTO =
        guestAuthMapper.toGuestAuthVerifyDTO(guestAuthVerifyRequest);
    GuestTokenDTO guestTokenDTO = guestAuthService.verifyGuestAuthCode(guestAuthVerifyDTO);

    // 게스트 토큰 쿠키 설정
    tokenCookieService.addGuestTokenCookie(response, guestTokenDTO.getGuestToken());

    return responseHelper.success(null, "비회원 전화번호 인증 검증이 성공적으로 완료되었습니다.", HttpStatus.OK);
  }
}
