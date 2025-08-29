package liaison.groble.api.server.guest;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.order.request.CreateOrderRequest;
import liaison.groble.api.model.order.response.CreateOrderResponse;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.order.service.OrderService;
import liaison.groble.application.terms.dto.TermsAgreementDTO;
import liaison.groble.application.terms.service.OrderTermsService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.GuestOnly;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.order.OrderMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guest/order")
@Tag(name = "[👀 비회원] 비회원 주문/결제 기능", description = "비회원 주문 발행 기능")
public class GuestOrderController {
  // Service
  private final OrderService orderService;
  private final OrderTermsService orderTermsService;
  // Mapper
  private final OrderMapper orderMapper;
  // Helper
  private final ResponseHelper responseHelper;

  @GuestOnly
  @PostMapping("/create")
  @Logging(
      item = "GuestOrder",
      action = "createGuestOrder",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<CreateOrderResponse>> createGuestOrder(
      @Auth Accessor accessor,
      @Valid @RequestBody CreateOrderRequest request,
      HttpServletRequest httpRequest) {
    CreateOrderRequestDTO createOrderRequestDTO = orderMapper.toCreateOrderDTO(request);
    CreateOrderSuccessDTO createOrderSuccessDTO =
        orderService.createOrderForGuest(createOrderRequestDTO, accessor.getId());

    processGuestOrderTermsAgreement(accessor.getId(), httpRequest);
    CreateOrderResponse response = orderMapper.toCreateOrderResponse(createOrderSuccessDTO);
    return responseHelper.success(response, "비회원 주문 생성에 성공했습니다.", HttpStatus.CREATED);
  }

  /**
   * 비회원 주문 약관 동의 처리
   *
   * @param guestUserId 게스트 사용자 ID
   * @param httpRequest HTTP 요청 (IP, User-Agent 추출용)
   */
  private void processGuestOrderTermsAgreement(Long guestUserId, HttpServletRequest httpRequest) {
    try {
      TermsAgreementDTO termsAgreementDTO = toServiceOrderTermsAgreementDTO();
      // IP 및 User-Agent 설정
      termsAgreementDTO.setIpAddress(httpRequest.getRemoteAddr());
      termsAgreementDTO.setUserAgent(httpRequest.getHeader("User-Agent"));

      orderTermsService.agreeToOrderTermsForGuest(termsAgreementDTO, guestUserId);

      log.info("비회원 주문 약관 동의 처리 완료 - guestUserId: {}", guestUserId);
    } catch (Exception e) {
      log.error("비회원 주문 약관 동의 처리 실패 - guestUserId: {}", guestUserId, e);
      // 약관 동의 실패는 주문을 중단시키지 않음 (별도 처리 필요할 수 있음)
    }
  }

  private TermsAgreementDTO toServiceOrderTermsAgreementDTO() {
    List<String> termTypeStrs =
        List.of("ELECTRONIC_FINANCIAL", "PURCHASE_POLICY", "PERSONAL_INFORMATION");

    return TermsAgreementDTO.builder()
        .termsTypeStrings(termTypeStrs) // 문자열 리스트로 전달
        .build();
  }
}
