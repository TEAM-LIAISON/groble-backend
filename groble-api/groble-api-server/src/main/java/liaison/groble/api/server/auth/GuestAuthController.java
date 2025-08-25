package liaison.groble.api.server.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.application.auth.service.GuestAuthService;
import liaison.groble.common.response.ResponseHelper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 게스트 인증 관련 API 컨트롤러
 *
 * <p>(1) 비회원 전화번호 인증 인증 요청
 *
 * <p>(2) 비회원 인증 토큰 유효성 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/guest")
@Tag(name = "[🙋🏻‍♂️ 게스트 - 비회원 결제 시스템]", description = "비회원 전화번호 인증 요청/토큰 유효성 확인 API")
public class GuestAuthController {
  // Service
  private final GuestAuthService guestAuthService;
  // Helper
  private final ResponseHelper responseHelper;
}
