package liaison.groble.api.server.order;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders/guest")
@Tag(name = "[🪄 비회원 주문] 비회원 주문 조회", description = "주문번호, 전화번호로 주문 상세/주문 목록을 조회합니다.")
public class GuestOrderController {
  //    @PostMapping("/search")
  //    public ResponseEntity<GrobleResponse<>> searchGuestOrder(
  //            @Valid @RequestBody GuestOrderSearchRequest request
  //    ) {
  //
  //    }
}
