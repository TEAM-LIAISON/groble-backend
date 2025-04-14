package liaison.groble.api.server.verification;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class VerificationController {

  /**
   * 이메일 인증 요청 API
   *
   * @param request 이메일 인증 요청 정보
   * @return 이메일 발송 결과
   */
  //    @PostMapping("/email/verification-request")
}
