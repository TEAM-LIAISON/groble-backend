package liaison.groble.mapping.order;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import liaison.groble.api.model.order.request.CreateOrderOptionRequest;
import liaison.groble.api.model.order.response.CreateOrderResponse;
import liaison.groble.application.order.dto.CreateOrderRequestDTO;
import liaison.groble.application.order.dto.CreateOrderSuccessDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-08T19:51:18+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public CreateOrderRequestDTO.OrderOptionDTO toOrderOptionDTO(CreateOrderOptionRequest request) {
        if ( request == null ) {
            return null;
        }

        CreateOrderRequestDTO.OrderOptionDTO.OrderOptionDTOBuilder orderOptionDTO = CreateOrderRequestDTO.OrderOptionDTO.builder();

        if ( request.getOptionId() != null ) {
            orderOptionDTO.optionId( request.getOptionId() );
        }
        if ( request.getQuantity() != null ) {
            orderOptionDTO.quantity( request.getQuantity() );
        }

        orderOptionDTO.optionType( mapOptionType(request.getOptionType()) );

        return orderOptionDTO.build();
    }

    @Override
    public List<CreateOrderRequestDTO.OrderOptionDTO> toOrderOptionDTOList(List<CreateOrderOptionRequest> requests) {
        if ( requests == null ) {
            return null;
        }

        List<CreateOrderRequestDTO.OrderOptionDTO> list = new ArrayList<CreateOrderRequestDTO.OrderOptionDTO>( requests.size() );
        for ( CreateOrderOptionRequest createOrderOptionRequest : requests ) {
            list.add( toOrderOptionDTO( createOrderOptionRequest ) );
        }

        return list;
    }

    @Override
    public CreateOrderResponse toCreateOrderResponse(CreateOrderSuccessDTO createOrderSuccessDTO) {
        if ( createOrderSuccessDTO == null ) {
            return null;
        }

        CreateOrderResponse.CreateOrderResponseBuilder createOrderResponse = CreateOrderResponse.builder();

        if ( createOrderSuccessDTO.getMerchantUid() != null ) {
            createOrderResponse.merchantUid( createOrderSuccessDTO.getMerchantUid() );
        }
        if ( createOrderSuccessDTO.getEmail() != null ) {
            createOrderResponse.email( createOrderSuccessDTO.getEmail() );
        }
        if ( createOrderSuccessDTO.getPhoneNumber() != null ) {
            createOrderResponse.phoneNumber( createOrderSuccessDTO.getPhoneNumber() );
        }
        if ( createOrderSuccessDTO.getContentTitle() != null ) {
            createOrderResponse.contentTitle( createOrderSuccessDTO.getContentTitle() );
        }
        if ( createOrderSuccessDTO.getTotalPrice() != null ) {
            createOrderResponse.totalPrice( createOrderSuccessDTO.getTotalPrice() );
        }
        if ( createOrderSuccessDTO.getIsPurchasedContent() != null ) {
            createOrderResponse.isPurchasedContent( createOrderSuccessDTO.getIsPurchasedContent() );
        }

        return createOrderResponse.build();
    }
}
