package liaison.groble.mapping.auth;

import org.mapstruct.Mapper;

import liaison.groble.api.model.auth.request.VerificationBusinessMakerAccountRequest;
import liaison.groble.api.model.auth.request.VerifyPersonalMakerAccountRequest;
import liaison.groble.application.auth.dto.VerifyBusinessMakerAccountDTO;
import liaison.groble.application.auth.dto.VerifyPersonalMakerAccountDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AccountVerificationMapper {
  VerifyPersonalMakerAccountDTO toVerifyPersonalMakerAccountDTO(
      VerifyPersonalMakerAccountRequest request);

  /** API 요청 모델 → 애플리케이션 DTO 매핑 (필드명이 같으면 자동, 다르면 @Mapping 필요) */
  VerifyBusinessMakerAccountDTO toVerifyBusinessMakerAccountDTO(
      VerificationBusinessMakerAccountRequest request);

  /** 서로 다른 enum 타입(BusinessType)을 이름(name) 기준으로 매핑 MapStruct가 자동으로 이 메서드를 호출합니다. */
  default VerifyBusinessMakerAccountDTO.BusinessType map(
      VerificationBusinessMakerAccountRequest.BusinessType businessType) {
    return businessType == null
        ? null
        : VerifyBusinessMakerAccountDTO.BusinessType.valueOf(businessType.name());
  }
}
