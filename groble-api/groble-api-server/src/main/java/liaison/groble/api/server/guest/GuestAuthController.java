package liaison.groble.api.server.guest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.guest.request.GuestAuthCodeRequest;
import liaison.groble.api.model.guest.request.GuestAuthVerifyRequest;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.dto.GuestAuthVerifyDTO;
import liaison.groble.application.guest.dto.GuestTokenDTO;
import liaison.groble.application.guest.service.GuestAuthService;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.TokenCookieService;
import liaison.groble.mapping.guest.GuestAuthMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guest/auth")
@Tag(name = "[👀 비회원] 비회원 인증/인가 기능", description = "비회원 토큰 발급, 비회원 전화번호 인증/검증")
public class GuestAuthController {

  // Service
  private final GuestAuthService guestAuthService;
  private final TokenCookieService tokenCookieService;

  // Mapper
  private final GuestAuthMapper guestAuthMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @PostMapping("/code-request")
  public ResponseEntity<GrobleResponse<Void>> sendGuestAuthCode(
      @Valid @RequestBody GuestAuthCodeRequest guestAuthCodeRequest) {
    GuestAuthDTO guestAuthDTO = guestAuthMapper.toGuestAuthDTO(guestAuthCodeRequest);
    guestAuthService.sendGuestAuthCode(guestAuthDTO);

    return responseHelper.success(null, "비회원 전화번호 인증 요청이 성공적으로 완료되었습니다.", HttpStatus.OK);
  }

  @PostMapping("/verify-request")
  public ResponseEntity<GrobleResponse<Void>> verifyGuestAuthCode(
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
