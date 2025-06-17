package liaison.groble.api.server.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "공통된 인증/인가 기능 관련 API", description = "닉네임, 전화번호 설정 API")
public class CommonAuthController {

  // 회원 탈퇴
}
