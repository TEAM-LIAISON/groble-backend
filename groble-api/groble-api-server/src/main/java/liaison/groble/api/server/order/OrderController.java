package liaison.groble.api.server.order;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.order.request.CreateInitialOrderRequest;
import liaison.groble.api.model.order.request.CreateOrderRequest;
import liaison.groble.api.model.order.response.OrderResponse;
import liaison.groble.api.server.order.mapper.OrderDtoMapper;
import liaison.groble.api.server.terms.mapper.TermsDtoMapper;
import liaison.groble.application.order.OrderService;
import liaison.groble.application.order.dto.CreateInitialOrderDto;
import liaison.groble.application.order.dto.InitialOrderResponse;
import liaison.groble.application.order.dto.OrderCreateDto;
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
@Tag(name = "Order", description = "주문 관련 API")
public class OrderController {

  private final OrderService orderService;
  private final OrderTermsService orderTermsService;
  private final OrderDtoMapper orderDtoMapper;
  private final TermsDtoMapper termsDtoMapper;

  @Operation(summary = "초기 주문 생성", description = "콘텐츠 구매를 위한 초기 주문을 생성합니다. 쿠폰 적용 불가능")
  @PostMapping("/initial")
  public ResponseEntity<GrobleResponse<InitialOrderResponse>> createInitialOrder(
      @Auth Accessor accessor, @Valid @RequestBody CreateInitialOrderRequest request) {

    // API 모델을 Application DTO로 변환
    CreateInitialOrderDto createInitialOrderDto =
        CreateInitialOrderDto.builder()
            .userId(accessor.getUserId())
            .contentId(request.getContentId())
            .options(convertToOptionDtos(request.getOptions()))
            .build();

    // 서비스 호출
    InitialOrderResponse response = orderService.createInitialOrder(createInitialOrderDto);

    return ResponseEntity.ok().body(GrobleResponse.success(response));
  }

  @Operation(summary = "최종 주문 발행", description = "콘텐츠 구매를 위한 최종 주문을 발행합니다. 쿠폰 적용 가능")
  @PostMapping("/{orderId}/finalize")
  public ResponseEntity<GrobleResponse<OrderResponse>> createOrder(
      @Auth Accessor accessor,
      @RequestBody CreateOrderRequest request,
      HttpServletRequest httpRequest) {

    if (!request.isOrderTermsAgreed()) {
      throw new InvalidRequestException("주문 약관에 동의해야 합니다.");
    }

    OrderCreateDto orderCreateDto =
        orderService.createOrder(
            accessor.getUserId(),
            request.getContentId(),
            request.getOptionType(),
            request.getOptionId(),
            request.getCouponCode());

    OrderResponse orderResponse = orderDtoMapper.toOrderResponse(orderCreateDto);

    TermsAgreementDto termsAgreementDto = termsDtoMapper.toServiceOrderTermsAgreementDto();
    termsAgreementDto.setUserId(accessor.getUserId());
    // IP 및 User-Agent 설정
    termsAgreementDto.setIpAddress(httpRequest.getRemoteAddr());
    termsAgreementDto.setUserAgent(httpRequest.getHeader("User-Agent"));
    orderTermsService.agreeToOrderTerms(termsAgreementDto);
    return ResponseEntity.ok().body(GrobleResponse.success(orderResponse));
  }

  // 변환 헬퍼 메서드
  private List<CreateInitialOrderDto.OrderOptionDto> convertToOptionDtos(
      List<CreateInitialOrderRequest.OrderOptionRequest> requests) {
    return requests.stream()
        .map(
            req ->
                CreateInitialOrderDto.OrderOptionDto.builder()
                    .optionId(req.getOptionId())
                    .optionType(
                        CreateInitialOrderDto.OptionType.valueOf(req.getOptionType().name()))
                    .quantity(req.getQuantity())
                    .build())
        .collect(Collectors.toList());
  }
}
