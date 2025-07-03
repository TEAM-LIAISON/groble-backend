package liaison.groble.mapping.terms;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.terms.enums.TermsTypeDTO;
import liaison.groble.api.model.terms.request.MakerTermsAgreementRequest;
import liaison.groble.api.model.terms.request.TermsAgreementRequest;
import liaison.groble.api.model.terms.response.TermsAgreementResponse;
import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;
import liaison.groble.application.terms.dto.TermsAgreementDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface TermsMapper {
  // ====== 📥 Request → DTO 변환 ======
  MakerTermsAgreementDTO toMakerTermsAgreementDTO(MakerTermsAgreementRequest request);

  @Mapping(
      target = "termsTypeStrings",
      expression = "java(mapTermsTypesToStrings(request.getTermsTypes()))")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "typeString", ignore = true)
  @Mapping(target = "title", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "required", ignore = true)
  @Mapping(target = "contentUrl", ignore = true)
  @Mapping(target = "agreed", ignore = true)
  @Mapping(target = "agreedAt", ignore = true)
  @Mapping(target = "ipAddress", ignore = true)
  @Mapping(target = "userAgent", ignore = true)
  @Mapping(target = "effectiveFrom", ignore = true)
  @Mapping(target = "effectiveTo", ignore = true)
  TermsAgreementDTO toTermsAgreementDTO(TermsAgreementRequest request);

  // ====== 📤 DTO → Response 변환 ======
  @Mapping(target = "type", expression = "java(mapStringToTermsType(dto.getTypeString()))")
  TermsAgreementResponse toTermsAgreementResponse(TermsAgreementDTO dto);

  default List<String> mapTermsTypesToStrings(List<TermsTypeDTO> termsTypes) {
    if (termsTypes == null) {
      return null;
    }
    return termsTypes.stream().map(TermsTypeDTO::name).toList();
  }

  default TermsTypeDTO mapStringToTermsType(String typeString) {
    if (typeString == null || typeString.isEmpty()) {
      return null;
    }
    try {
      return TermsTypeDTO.valueOf(typeString);
    } catch (IllegalArgumentException e) {
      return null; // 또는 기본값 반환
    }
  }
}
