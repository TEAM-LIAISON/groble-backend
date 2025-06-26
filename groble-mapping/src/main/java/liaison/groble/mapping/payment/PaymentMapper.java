package liaison.groble.mapping.payment;

import org.mapstruct.Mapper;

import liaison.groble.api.model.payment.request.PaypleAuthResultRequest;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface PaymentMapper {
  // ====== 📥 Request → DTO 변환 ======
  PaypleAuthResultDTO toPaypleAuthResultDTO(PaypleAuthResultRequest paypleAuthResultRequest);
  // ====== 📤 DTO → Response 변환 ======
}
