package liaison.groble.api.server.guest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.guest.request.GuestAuthRequest;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.service.GuestAuthService;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.guest.GuestAuthMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guest/auth")
@Tag(name = "[👀 비회원] 비회원 인증/인가 기능", description = "비회원 토큰 발급, 비회원 전화번호 인증/검증")
public class GuestAuthController {
  private final GuestAuthService guestAuthService;
  private final GuestAuthMapper guestAuthMapper;

  // Helper
  private final ResponseHelper responseHelper;

  // TODO: 비회원 인증번호 발송
  @PostMapping("/verify-request")
  public ResponseEntity<GrobleResponse<Void>> sendGuestAuthCode(GuestAuthRequest guestAuthRequest) {
    GuestAuthDTO guestAuthDTO = guestAuthMapper.toGuestAuthDTO(guestAuthRequest);
    guestAuthService.sendGuestAuthCode(guestAuthDTO);
    // 비회원 인증번호 발송 로직 구현
    return responseHelper.success(null, "비회원 전화번호 인증 요청이 성공적으로 완료되었습니다.", HttpStatus.OK);
  }

  // TODO: 비회원 인증번호 검증
  public ResponseEntity<GrobleResponse<Void>> verifyGuestAuthCode(
      String phoneNumber, String authCode) {
    // 비회원 인증번호 검증 로직 구현
    return null;
  }
}
