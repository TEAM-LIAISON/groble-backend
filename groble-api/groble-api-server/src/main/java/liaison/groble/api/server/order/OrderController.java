package liaison.groble.api.server.order;

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
import liaison.groble.api.server.terms.mapper.TermsDtoMapper;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.order.dto.OrderSuccessDTO;
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
    name = "[🪄 주문] 결제창에서 주문 발행, 주문 결과 조회 API",
    description = "결제 직전 주문 정보를 생성합니다. 주문 성공 페이지 정보를 조회합니다.")
public class OrderController {

  // API 경로 상수화
  private static final String CREATE_ORDER_PATH = "/create";
  private static final String ORDER_SUCCESS_PATH = "/success/{merchantUid}";

  // 응답 메시지 상수화
  private static final String CREATE_ORDER_SUCCESS_MESSAGE = "주문 생성에 성공했습니다.";
  private static final String ORDER_SUCCESS_RESPONSE_MESSAGE = "주문 성공 페이지 정보 조회에 성공했습니다.";

  // Service
  private final OrderService orderService;
  private final OrderTermsService orderTermsService;
  private final TermsDtoMapper termsDtoMapper;

  // Mapper
  private final OrderMapper orderMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[✅ 주문 발행] 결제 창에서 주문 발행",
      description =
          "결제 창에서 회원이 merchantUid를 받기 위해 실행." + "콘텐츠 구매를 위한 결제 주문을 발행합니다. 회원은 쿠폰 사용이 가능합니다.")
  @ApiResponse(
      responseCode = "200",
      description = CREATE_ORDER_SUCCESS_MESSAGE,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CreateOrderResponse.class)))
  @PostMapping(CREATE_ORDER_PATH)
  @Logging(item = "Order", action = "createOrder", includeParam = true, includeResult = true)
  public ResponseEntity<GrobleResponse<CreateOrderResponse>> createOrder(
      @Auth Accessor accessor,
      @Valid @RequestBody CreateOrderRequest request,
      HttpServletRequest httpRequest) {
    CreateOrderRequestDTO createOrderRequestDTO = orderMapper.toCreateOrderDTO(request);

    CreateOrderSuccessDTO createOrderSuccessDTO =
        orderService.createOrderForUser(createOrderRequestDTO, accessor.getUserId());

    processOrderTermsAgreement(accessor.getUserId(), httpRequest);
    CreateOrderResponse response = orderMapper.toCreateOrderResponse(createOrderSuccessDTO);
    return responseHelper.success(response, CREATE_ORDER_SUCCESS_MESSAGE, HttpStatus.CREATED);
  }

  @Operation(
      summary = "[✅ 주문 조회] 결제 성공 페이지 정보(주문 상세 정보) 조회",
      description = "결제 완료된 주문의 상세 정보를 조회합니다. 상품 정보, 결제 금액, 구매 일시 등을 포함합니다.")
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
      @Auth Accessor accessor, @Valid @PathVariable("merchantUid") String merchantUid) {

    OrderSuccessDTO orderSuccessDTO =
        orderService.getOrderSuccess(merchantUid, accessor.getUserId());

    OrderSuccessResponse orderSuccessResponse = orderMapper.toOrderSuccessResponse(orderSuccessDTO);

    return responseHelper.success(
        orderSuccessResponse, ORDER_SUCCESS_RESPONSE_MESSAGE, HttpStatus.OK);
  }

  /**
   * 주문 약관 동의 처리 (회원 전용)
   *
   * @param userId 사용자 ID
   * @param httpRequest HTTP 요청 (IP, User-Agent 추출용)
   */
  private void processOrderTermsAgreement(Long userId, HttpServletRequest httpRequest) {
    try {
      TermsAgreementDTO termsAgreementDto = termsDtoMapper.toServiceOrderTermsAgreementDto();
      termsAgreementDto.setUserId(userId);
      // IP 및 User-Agent 설정
      termsAgreementDto.setIpAddress(httpRequest.getRemoteAddr());
      termsAgreementDto.setUserAgent(httpRequest.getHeader("User-Agent"));

      orderTermsService.agreeToOrderTerms(termsAgreementDto);

      log.info("주문 약관 동의 처리 완료 - userId: {}", userId);
    } catch (Exception e) {
      log.error("주문 약관 동의 처리 실패 - userId: {}", userId, e);
      // 약관 동의 실패는 주문을 중단시키지 않음 (별도 처리 필요할 수 있음)
    }
  }
}
