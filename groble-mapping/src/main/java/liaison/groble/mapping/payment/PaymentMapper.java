package liaison.groble.mapping.payment;

import org.mapstruct.Mapper;

import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaypleAuthResultRequest;
import liaison.groble.api.model.payment.response.PaymentCancelInfoResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelInfoDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface PaymentMapper {
  // ====== 📥 Request → DTO 변환 ======
  PaypleAuthResultDTO toPaypleAuthResultDTO(PaypleAuthResultRequest paypleAuthResultRequest);

  PaymentCancelDTO toPaymentCancelDTO(PaymentCancelRequest paymentCancelRequest);

  // ====== 📤 DTO → Response 변환 ======
  PaymentCancelInfoResponse toPaymentCancelInfoResponse(PaymentCancelInfoDTO paymentCancelInfoDTO);
}
