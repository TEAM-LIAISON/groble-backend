package liaison.groble.mapping.terms;

import java.util.List;

import org.mapstruct.Mapper;

import liaison.groble.api.model.terms.enums.TermsTypeDTO;
import liaison.groble.api.model.terms.request.MakerTermsAgreementRequest;
import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface TermsMapper {
  // ====== 📥 Request → DTO 변환 ======
  MakerTermsAgreementDTO toMakerTermsAgreementDTO(MakerTermsAgreementRequest request);

  // ====== 📤 DTO → Response 변환 ======
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
