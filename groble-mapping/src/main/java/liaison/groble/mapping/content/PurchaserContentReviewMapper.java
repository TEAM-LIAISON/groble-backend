package liaison.groble.mapping.content;

import org.mapstruct.Mapper;

import liaison.groble.api.model.purchase.request.PurchaserContentReviewRequest;
import liaison.groble.api.model.purchase.response.PurchaserContentReviewResponse;
import liaison.groble.application.purchase.dto.PurchaserContentReviewDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface PurchaserContentReviewMapper {
  // ====== 📥 Request → DTO 변환 ======
  PurchaserContentReviewDTO toPurchaserContentReviewDTO(
      PurchaserContentReviewRequest purchaserContentReviewRequest);

  // ====== 📤 DTO → Response 변환 ======
  PurchaserContentReviewResponse toPurchaserContentReviewResponse(
      PurchaserContentReviewDTO purchaserContentReviewDTO);
}
