package liaison.groble.api.server.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.application.auth.service.AccountService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
@Tag(name = "로그아웃 및 회원탈퇴와 같은 공통 인증/인가 관련 API", description = "로그아웃 및 회원가입 기능 API")
public class AccountController {

  private final AccountService accountService;
}
