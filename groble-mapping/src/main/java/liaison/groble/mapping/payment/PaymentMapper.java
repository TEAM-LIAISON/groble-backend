package liaison.groble.mapping.payment;

import org.mapstruct.Mapper;

import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface PaymentMapper {
  // ====== 📥 Request → DTO 변환 ======

  // ====== 📤 DTO → Response 변환 ======
}
