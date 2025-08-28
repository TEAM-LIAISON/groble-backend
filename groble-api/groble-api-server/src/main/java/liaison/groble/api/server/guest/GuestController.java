package liaison.groble.api.server.guest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guest")
@Tag(name = "[👀 비회원] 비회원 기능", description = "비회원 주문조회, 비회원 결제 등 비회원 관련 기능")
public class GuestController {
  // TODO: 비회원 주문조회
  // TODO: 비회원 결제
  // TODO: 비회원 인증번호 발송
  // TODO: 비회원 인증번호 검증
  // TODO: 비회원 리뷰 작성
  // TODO: 비회원 콘텐츠 다운로드
}
