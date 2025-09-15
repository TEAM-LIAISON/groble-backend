package liaison.groble.mapping.order;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.order.request.CreateOrderOptionRequest;
import liaison.groble.api.model.order.request.CreateOrderRequest;
import liaison.groble.api.model.order.response.CreateOrderResponse;
import liaison.groble.api.model.order.response.OrderSuccessResponse;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import liaison.groble.application.order.dto.OrderSuccessDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface OrderMapper {
  // ====== 📤 Request → DTO 변환 ======

  // CreateOrderDTO는 static factory method 사용하므로 default 메서드로 구현
  default CreateOrderRequestDTO toCreateOrderDTO(CreateOrderRequest request) {
    return CreateOrderRequestDTO.of(
        request.getContentId(),
        toOrderOptionDTOList(request.getOptions()),
        request.getCouponCodes(),
        request.isBuyerInfoStorageAgreed());
  }

  @Mapping(target = "optionType", expression = "java(mapOptionType(request.getOptionType()))")
  CreateOrderRequestDTO.OrderOptionDTO toOrderOptionDTO(CreateOrderOptionRequest request);

  List<CreateOrderRequestDTO.OrderOptionDTO> toOrderOptionDTOList(
      List<CreateOrderOptionRequest> requests);

  // Enum 매핑 헬퍼 메서드
  default CreateOrderRequestDTO.OrderOptionDTO.OptionType mapOptionType(
      CreateOrderOptionRequest.OptionType requestType) {
    return CreateOrderRequestDTO.OrderOptionDTO.OptionType.valueOf(requestType.name());
  }

  // ====== 📤 DTO → Response 변환 ======
  CreateOrderResponse toCreateOrderResponse(CreateOrderSuccessDTO createOrderSuccessDTO);

  OrderSuccessResponse toOrderSuccessResponse(OrderSuccessDTO orderSuccessDTO);
}
