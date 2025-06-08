package liaison.groble.api.server.order;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.order.request.CreateOrderRequest;
import liaison.groble.api.server.terms.mapper.TermsDtoMapper;
import liaison.groble.application.order.dto.CreateOrderDto;
import liaison.groble.application.order.dto.CreateOrderResponse;
import liaison.groble.application.order.dto.OrderSuccessResponse;
import liaison.groble.application.order.service.OrderService;
import liaison.groble.application.terms.dto.TermsAgreementDto;
import liaison.groble.application.terms.service.OrderTermsService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.exception.InvalidRequestException;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "주문 관련 API", description = "회원/비회원 주문 생성 API")
public class OrderController {

  private final OrderService orderService;
  private final OrderTermsService orderTermsService;
  private final TermsDtoMapper termsDtoMapper;

  @Operation(
      summary = "결제 주문 발행",
      description = "콘텐츠 구매를 위한 결제 주문을 발행합니다. 회원은 쿠폰 적용 가능, 비회원은 이메일/전화번호 필수")
  @PostMapping("/create")
  public ResponseEntity<GrobleResponse<CreateOrderResponse>> createOrder(
      @Auth Accessor accessor,
      @Valid @RequestBody CreateOrderRequest request,
      HttpServletRequest httpRequest) {
    CreateOrderResponse response;

    response = processAuthenticatedOrder(request, accessor);

    // 회원 주문 약관 동의 처리
    processOrderTermsAgreement(accessor.getUserId(), httpRequest);

    log.info(
        "주문 생성 완료 - merchantUid: {}, isAuthenticated: {}",
        response.getMerchantUid(),
        accessor.isAuthenticated());

    return ResponseEntity.ok(GrobleResponse.success(response));
  }

  @Operation(
      summary = "결제 성공 페이지 정보 조회",
      description = "결제 완료된 주문의 상세 정보를 조회합니다. 상품 정보, 결제 금액, 구매 일시 등을 포함합니다.")
  @GetMapping("/success/{merchantUid}")
  public ResponseEntity<GrobleResponse<OrderSuccessResponse>> getSuccessOrderPage(
      @Auth Accessor accessor, @Valid @PathVariable("merchantUid") String merchantUid) {

    try {
      OrderSuccessResponse response =
          orderService.getOrderSuccess(merchantUid, accessor.getUserId());

      log.info(
          "주문 성공 정보 조회 완료 - merchantUid: {}, userId: {}, contentTitle: {}",
          merchantUid,
          accessor.getUserId(),
          response.getContentTitle());

      return ResponseEntity.ok(GrobleResponse.success(response));

    } catch (IllegalArgumentException e) {
      log.warn("주문을 찾을 수 없음 - merchantUid: {}, userId: {}", merchantUid, accessor.getUserId());
      throw new InvalidRequestException("주문 정보를 찾을 수 없습니다.");

    } catch (IllegalStateException e) {
      log.warn(
          "주문 접근 권한 없음 - merchantUid: {}, userId: {}, error: {}",
          merchantUid,
          accessor.getUserId(),
          e.getMessage());
      throw new InvalidRequestException("해당 주문에 접근할 수 없습니다.");

    } catch (Exception e) {
      log.error(
          "주문 성공 정보 조회 실패 - merchantUid: {}, userId: {}", merchantUid, accessor.getUserId(), e);
      throw new InvalidRequestException("주문 정보 조회 중 오류가 발생했습니다.");
    }
  }

  /**
   * 회원 주문 처리
   *
   * @param request 주문 요청 정보
   * @param accessor 인증된 사용자 정보
   * @return 주문 생성 응답
   */
  private CreateOrderResponse processAuthenticatedOrder(
      CreateOrderRequest request, Accessor accessor) {
    CreateOrderDto dto = convertToCreateOrderDto(request);
    return orderService.createOrderForUser(dto, accessor.getUserId());
  }

  private CreateOrderDto convertToCreateOrderDto(CreateOrderRequest request) {
    List<CreateOrderDto.OrderOptionDto> optionDtos =
        request.getOptions().stream()
            .map(
                opt ->
                    CreateOrderDto.OrderOptionDto.builder()
                        .optionId(opt.getOptionId())
                        .optionType(
                            CreateOrderDto.OrderOptionDto.OptionType.valueOf(
                                opt.getOptionType().name()))
                        .quantity(opt.getQuantity())
                        .build())
            .toList();

    return CreateOrderDto.of(request.getContentId(), optionDtos, request.getCouponCodes());
  }

  /**
   * 주문 약관 동의 처리 (회원 전용)
   *
   * @param userId 사용자 ID
   * @param httpRequest HTTP 요청 (IP, User-Agent 추출용)
   */
  private void processOrderTermsAgreement(Long userId, HttpServletRequest httpRequest) {
    try {
      TermsAgreementDto termsAgreementDto = termsDtoMapper.toServiceOrderTermsAgreementDto();
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
