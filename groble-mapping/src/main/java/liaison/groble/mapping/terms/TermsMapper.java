package liaison.groble.mapping.terms;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.terms.enums.TermsTypeDTO;
import liaison.groble.api.model.terms.request.MakerTermsAgreementRequest;
import liaison.groble.api.model.terms.request.TermsAgreementRequest;
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

  default List<String> mapTermsTypesToStrings(List<TermsTypeDTO> termsTypes) {
    if (termsTypes == null) {
      return null;
    }
    return termsTypes.stream().map(TermsTypeDTO::name).toList();
  }
}
