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
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.order.docs.OrderSwaggerDocs;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.order.dto.OrderSuccessDTO;
import liaison.groble.application.order.strategy.OrderProcessorFactory;
import liaison.groble.application.order.strategy.OrderProcessorStrategy;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.factory.UserContextFactory;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.order.OrderMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Order.BASE)
@Tag(name = OrderSwaggerDocs.TAG_NAME, description = OrderSwaggerDocs.TAG_DESCRIPTION)
public class OrderController extends BaseController {

  // Factory
  private final OrderProcessorFactory processorFactory;

  // Mapper
  private final OrderMapper orderMapper;

  public OrderController(
      ResponseHelper responseHelper,
      OrderProcessorFactory processorFactory,
      OrderMapper orderMapper) {
    super(responseHelper);
    this.processorFactory = processorFactory;
    this.orderMapper = orderMapper;
  }

  @Operation(
      summary = OrderSwaggerDocs.CREATE_ORDER_SUMMARY,
      description = OrderSwaggerDocs.CREATE_ORDER_DESCRIPTION)
  @PostMapping(ApiPaths.Order.CREATE_ORDER)
  @Logging(item = "Order", action = "createOrder", includeParam = true, includeResult = true)
  public ResponseEntity<GrobleResponse<CreateOrderResponse>> createOrder(
      @Auth(required = false) Accessor accessor,
      @Valid @RequestBody CreateOrderRequest request,
      HttpServletRequest httpRequest) {

    CreateOrderRequestDTO createOrderRequestDTO = orderMapper.toCreateOrderDTO(request);
    UserContext userContext = UserContextFactory.from(accessor);

    // UserContext와 Strategy 패턴을 이용한 주문 처리
    OrderProcessorStrategy processor = processorFactory.getProcessor(userContext);
    CreateOrderSuccessDTO createOrderSuccessDTO =
        processor.createOrder(userContext, createOrderRequestDTO, httpRequest);

    String userTypeInfo = userContext.isMember() ? "회원" : "비회원";
    log.info("{} 주문 처리 완료 - userId: {}", userTypeInfo, userContext.getId());

    CreateOrderResponse response = orderMapper.toCreateOrderResponse(createOrderSuccessDTO);
    log.info("{} 주문 생성 완료 - merchantUid: {}", userTypeInfo, createOrderSuccessDTO.getMerchantUid());

    return success(response, ResponseMessages.Order.ORDER_CREATE_SUCCESS, HttpStatus.CREATED);
  }

  @Operation(
      summary = OrderSwaggerDocs.GET_ORDER_SUMMARY,
      description = OrderSwaggerDocs.GET_ORDER_DESCRIPTION)
  @Logging(
      item = "Order",
      action = "getSuccessOrderPage",
      includeParam = true,
      includeResult = true)
  @GetMapping(ApiPaths.Order.GET_ORDER_SUCCESS)
  public ResponseEntity<GrobleResponse<OrderSuccessResponse>> getSuccessOrderPage(
      @Auth(required = false) Accessor accessor,
      @Valid @PathVariable("merchantUid") String merchantUid) {

    UserContext userContext = UserContextFactory.from(accessor);

    // UserContext와 Strategy 패턴을 이용한 주문 조회
    OrderProcessorStrategy processor = processorFactory.getProcessor(userContext);
    OrderSuccessDTO orderSuccessDTO = processor.getOrderSuccess(userContext, merchantUid);

    String userTypeInfo = userContext.isMember() ? "회원" : "비회원";
    log.info(
        "{} 주문 성공 페이지 조회 - userId: {}, merchantUid: {}",
        userTypeInfo,
        userContext.getId(),
        merchantUid);

    OrderSuccessResponse orderSuccessResponse = orderMapper.toOrderSuccessResponse(orderSuccessDTO);
    return success(orderSuccessResponse, ResponseMessages.Order.GET_ORDER_SUCCESS);
  }
}
