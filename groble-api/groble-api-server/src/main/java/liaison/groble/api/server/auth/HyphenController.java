package liaison.groble.api.server.auth;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "하이픈 계좌 인증 API", description = "하이픈 계좌 인증 관련 API (개인 메이커 인증 & 개인 • 법인 사업자)")
public class HyphenController {}
