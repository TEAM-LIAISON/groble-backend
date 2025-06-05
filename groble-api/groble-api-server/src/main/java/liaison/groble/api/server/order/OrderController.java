package liaison.groble.api.server.order;

import java.util.List;
import java.util.stream.Collectors;

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
import liaison.groble.application.order.OrderService;
import liaison.groble.application.order.dto.CreateOrderDto;
import liaison.groble.application.order.dto.CreateOrderResponse;
import liaison.groble.application.order.dto.OrderSuccessResponse;
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
      summary = "결제 성공 페이지 정보 조회",
      description = "결제 완료된 주문의 상세 정보를 조회합니다. 상품 정보, 결제 금액, 구매 일시 등을 포함합니다.")
  @GetMapping("/success/{merchantUid}")
  public ResponseEntity<GrobleResponse<OrderSuccessResponse>> getSuccessOrderPage(
      @Auth Accessor accessor, @Valid @PathVariable String merchantUid) {

    // 인증된 사용자만 접근 가능
    if (!accessor.isAuthenticated()) {
      throw new InvalidRequestException("로그인이 필요합니다.");
    }

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

  @Operation(
      summary = "결제 주문 발행",
      description = "콘텐츠 구매를 위한 결제 주문을 발행합니다. 회원은 쿠폰 적용 가능, 비회원은 이메일/전화번호 필수")
  @PostMapping("/create")
  public ResponseEntity<GrobleResponse<CreateOrderResponse>> createOrder(
      @Auth Accessor accessor,
      @Valid @RequestBody CreateOrderRequest request,
      HttpServletRequest httpRequest) {

    // 1단계: 주문 약관 동의 검증
    if (!request.isOrderTermsAgreed()) {
      throw new InvalidRequestException("주문 약관에 동의해야 합니다.");
    }

    CreateOrderResponse response;

    // 회원 주문 처리
    response = processAuthenticatedOrder(request, accessor);

    // 회원 주문 약관 동의 처리
    processOrderTermsAgreement(accessor.getUserId(), httpRequest);

    //    } else {
    //      // 비회원 주문 처리
    //      response = processGuestOrder(request);
    //
    //      // 비회원은 주문 약관 동의를 별도 저장하지 않음 (요청 시점에만 확인)
    //      log.info("비회원 주문 약관 동의 확인 완료 - email: {}", request.getEmail());
    //    }

    log.info(
        "주문 생성 완료 - merchantUid: {}, isAuthenticated: {}",
        response.getMerchantUid(),
        accessor.isAuthenticated());

    return ResponseEntity.ok(GrobleResponse.success(response));
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
    CreateOrderDto createOrderDto =
        CreateOrderDto.builder()
            .contentId(request.getContentId())
            .options(convertToOrderOptionDtos(request.getOptions()))
            .couponCodes(request.getCouponCodes())
            .build();

    return orderService.createOrderForUser(createOrderDto, accessor.getUserId());
  }

  /**
   * 비회원 주문 처리
   *
   * @param request 주문 요청 정보 (이메일, 전화번호 포함)
   * @return 주문 생성 응답
   */
  private CreateOrderResponse processGuestOrder(CreateOrderRequest request) {
    // 비회원 주문 필수 정보 검증
    //    if (request.getEmail() == null || request.getEmail().isBlank()) {
    //      throw new InvalidRequestException("비회원 주문 시 이메일은 필수입니다.");
    //    }
    //    if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
    //      throw new InvalidRequestException("비회원 주문 시 전화번호는 필수입니다.");
    //    }

    // 비회원은 쿠폰 사용 불가
    if (request.getCouponCodes() != null && !request.getCouponCodes().isEmpty()) {
      throw new InvalidRequestException("비회원은 쿠폰을 사용할 수 없습니다.");
    }

    CreateOrderDto createOrderDto =
        CreateOrderDto.builder()
            .contentId(request.getContentId())
            .options(convertToOrderOptionDtos(request.getOptions()))
            .couponCodes(null) // 비회원은 쿠폰 사용 불가
            .build();

    return orderService.createPublicOrder(createOrderDto);
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

  /**
   * 요청 DTO를 서비스 DTO로 변환
   *
   * @param requests 주문 옵션 요청 리스트
   * @return 변환된 주문 옵션 DTO 리스트
   */
  private List<CreateOrderDto.OrderOptionDto> convertToOrderOptionDtos(
      List<CreateOrderRequest.OrderOptionRequest> requests) {
    return requests.stream()
        .map(
            req ->
                CreateOrderDto.OrderOptionDto.builder()
                    .optionId(req.getOptionId())
                    .optionType(CreateOrderDto.OptionType.valueOf(req.getOptionType().name()))
                    .quantity(req.getQuantity())
                    .build())
        .collect(Collectors.toList());
  }
}
