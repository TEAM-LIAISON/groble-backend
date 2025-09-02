package liaison.groble.api.server.order;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.order.request.CreateOrderRequest;
import liaison.groble.api.model.order.response.CreateOrderResponse;
import liaison.groble.api.model.order.response.OrderSuccessResponse;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.order.dto.OrderSuccessDTO;
import liaison.groble.application.order.exception.OrderAuthenticationRequiredException;
import liaison.groble.application.order.service.OrderService;
import liaison.groble.application.terms.dto.TermsAgreementDTO;
import liaison.groble.application.terms.service.OrderTermsService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.order.OrderMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(
    name = "[🔄 통합 주문] 회원/비회원 통합 주문 발행, 회원/비회원 주문 결과 조회 API",
    description = "토큰 종류에 따라 회원/비회원을 자동 판단하여 주문을 처리합니다.")
public class OrderController {

  // API 경로 상수화
  private static final String ORDER_SUCCESS_PATH = "/success/{merchantUid}";

  // 응답 메시지 상수화
  private static final String CREATE_ORDER_SUCCESS_MESSAGE = "주문 생성에 성공했습니다.";
  private static final String ORDER_SUCCESS_RESPONSE_MESSAGE = "주문 성공 페이지 정보 조회에 성공했습니다.";

  // Service
  private final OrderService orderService;
  private final OrderTermsService orderTermsService;

  // Mapper
  private final OrderMapper orderMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[✅ 통합 주문 발행] 회원/비회원 자동 판단 주문 발행",
      description =
          "토큰 종류에 따라 회원(accessToken)과 비회원(guestToken)을 자동 판단하여 주문을 발행합니다. "
              + "회원은 쿠폰 사용이 가능하며, 비회원은 전화번호 인증 후 이용 가능합니다.")
  @ApiResponse(
      responseCode = "201",
      description = "CREATE_ORDER_SUCCESS_MESSAGE",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CreateOrderResponse.class)))
  @PostMapping("/create")
  @Logging(item = "Order", action = "createOrder", includeParam = true, includeResult = true)
  public ResponseEntity<GrobleResponse<CreateOrderResponse>> createOrder(
      @Auth(required = false) Accessor accessor,
      @Valid @RequestBody CreateOrderRequest request,
      HttpServletRequest httpRequest) {
    CreateOrderRequestDTO createOrderRequestDTO = orderMapper.toCreateOrderDTO(request);
    CreateOrderSuccessDTO createOrderSuccessDTO;
    String userTypeInfo;
    // 토큰 종류에 따른 분기 처리
    if (accessor.isAuthenticated() && !accessor.isGuest()) {
      // 회원 주문 처리 (accessToken)
      log.info(
          "회원 주문 처리 시작 - userId: {}, userType: {}", accessor.getUserId(), accessor.getUserType());
      createOrderSuccessDTO =
          orderService.createOrderForUser(createOrderRequestDTO, accessor.getUserId());
      processOrderTermsAgreement(accessor.getUserId(), httpRequest);
      userTypeInfo = "회원";

    } else if (accessor.isGuest()) {
      // 비회원 주문 처리 (guestToken)
      log.info("비회원 주문 처리 시작 - guestUserId: {}", accessor.getId());
      createOrderSuccessDTO =
          orderService.createOrderForGuest(createOrderRequestDTO, accessor.getId());
      processGuestOrderTermsAgreement(accessor.getId(), httpRequest);
      userTypeInfo = "비회원";

    } else {
      // 인증되지 않은 사용자
      throw OrderAuthenticationRequiredException.forOrderCreation();
    }

    CreateOrderResponse response = orderMapper.toCreateOrderResponse(createOrderSuccessDTO);
    log.info("{} 주문 생성 완료 - merchantUid: {}", userTypeInfo, createOrderSuccessDTO.getMerchantUid());

    return responseHelper.success(
        response, userTypeInfo + " " + CREATE_ORDER_SUCCESS_MESSAGE, HttpStatus.CREATED);
  }

  @Operation(
      summary = "[✅ 통합 주문 조회] 회원/비회원 주문 성공 페이지 정보 조회",
      description = "토큰 종류에 따라 회원/비회원을 자동 판단하여 주문 성공 정보를 조회합니다.")
  @ApiResponse(
      responseCode = "200",
      description = ORDER_SUCCESS_RESPONSE_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OrderSuccessResponse.class)))
  @GetMapping(ORDER_SUCCESS_PATH)
  @Logging(
      item = "Order",
      action = "getSuccessOrderPage",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<OrderSuccessResponse>> getSuccessOrderPage(
      @Auth(required = false) Accessor accessor,
      @Valid @PathVariable("merchantUid") String merchantUid) {

    OrderSuccessDTO orderSuccessDTO;

    if (accessor.isAuthenticated() && !accessor.isGuest()) {
      // 회원 주문 조회
      orderSuccessDTO = orderService.getOrderSuccess(merchantUid, accessor.getUserId());
      log.info("회원 주문 성공 페이지 조회 - userId: {}, merchantUid: {}", accessor.getUserId(), merchantUid);

    } else if (accessor.isGuest()) {
      // 비회원 주문 조회
      orderSuccessDTO = orderService.getGuestOrderSuccess(merchantUid, accessor.getId());
      log.info(
          "비회원 주문 성공 페이지 조회 - guestUserId: {}, merchantUid: {}", accessor.getId(), merchantUid);

    } else {
      throw OrderAuthenticationRequiredException.forOrderInquiry();
    }

    OrderSuccessResponse orderSuccessResponse = orderMapper.toOrderSuccessResponse(orderSuccessDTO);
    return responseHelper.success(
        orderSuccessResponse, ORDER_SUCCESS_RESPONSE_MESSAGE, HttpStatus.OK);
  }

  /**
   * 회원 주문 약관 동의 처리
   *
   * @param userId 사용자 ID
   * @param httpRequest HTTP 요청 (IP, User-Agent 추출용)
   */
  private void processOrderTermsAgreement(Long userId, HttpServletRequest httpRequest) {
    try {
      TermsAgreementDTO termsAgreementDTO = createTermsAgreementDTO();
      termsAgreementDTO.setUserId(userId);
      // IP 및 User-Agent 설정
      termsAgreementDTO.setIpAddress(httpRequest.getRemoteAddr());
      termsAgreementDTO.setUserAgent(httpRequest.getHeader("User-Agent"));

      orderTermsService.agreeToOrderTerms(termsAgreementDTO);
      log.info("회원 주문 약관 동의 처리 완료 - userId: {}", userId);

    } catch (Exception e) {
      log.error("회원 주문 약관 동의 처리 실패 - userId: {}", userId, e);
      // 약관 동의 실패는 주문을 중단시키지 않음 (별도 처리 필요할 수 있음)
    }
  }

  /**
   * 비회원 주문 약관 동의 처리
   *
   * @param guestUserId 게스트 사용자 ID
   * @param httpRequest HTTP 요청 (IP, User-Agent 추출용)
   */
  private void processGuestOrderTermsAgreement(Long guestUserId, HttpServletRequest httpRequest) {
    try {
      TermsAgreementDTO termsAgreementDTO = createTermsAgreementDTO();
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

  /** 공통 약관 동의 DTO 생성 */
  private TermsAgreementDTO createTermsAgreementDTO() {
    List<String> termTypeStrs =
        List.of("ELECTRONIC_FINANCIAL", "PURCHASE_POLICY", "PERSONAL_INFORMATION");

    return TermsAgreementDTO.builder()
        .termsTypeStrings(termTypeStrs) // 문자열 리스트로 전달
        .build();
  }
}
