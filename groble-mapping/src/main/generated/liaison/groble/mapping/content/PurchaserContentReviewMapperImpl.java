package liaison.groble.mapping.content;

import javax.annotation.processing.Generated;
import liaison.groble.api.model.purchase.request.PurchaserContentReviewRequest;
import liaison.groble.api.model.purchase.response.PurchaserContentReviewResponse;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-21T12:05:29+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)"
)
@Component
public class PurchaserContentReviewMapperImpl implements PurchaserContentReviewMapper {

    @Override
    public PurchaserContentReviewDTO toPurchaserContentReviewDTO(PurchaserContentReviewRequest purchaserContentReviewRequest) {
        if ( purchaserContentReviewRequest == null ) {
            return null;
        }

        PurchaserContentReviewDTO.PurchaserContentReviewDTOBuilder purchaserContentReviewDTO = PurchaserContentReviewDTO.builder();

        if ( purchaserContentReviewRequest.getRating() != null ) {
            purchaserContentReviewDTO.rating( purchaserContentReviewRequest.getRating() );
        }
        if ( purchaserContentReviewRequest.getReviewContent() != null ) {
            purchaserContentReviewDTO.reviewContent( purchaserContentReviewRequest.getReviewContent() );
        }

        return purchaserContentReviewDTO.build();
    }

    @Override
    public PurchaserContentReviewResponse toPurchaserContentReviewResponse(PurchaserContentReviewDTO purchaserContentReviewDTO) {
        if ( purchaserContentReviewDTO == null ) {
            return null;
        }

        PurchaserContentReviewResponse.PurchaserContentReviewResponseBuilder purchaserContentReviewResponse = PurchaserContentReviewResponse.builder();

        if ( purchaserContentReviewDTO.getRating() != null ) {
            purchaserContentReviewResponse.rating( purchaserContentReviewDTO.getRating() );
        }
        if ( purchaserContentReviewDTO.getReviewContent() != null ) {
            purchaserContentReviewResponse.reviewContent( purchaserContentReviewDTO.getReviewContent() );
        }

        return purchaserContentReviewResponse.build();
    }
}
