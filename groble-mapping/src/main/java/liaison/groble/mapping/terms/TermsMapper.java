package liaison.groble.mapping.terms;

import org.mapstruct.Mapper;

import liaison.groble.api.model.terms.request.MakerTermsAgreementRequest;
import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface TermsMapper {
  // ====== 📥 Request → DTO 변환 ======
  MakerTermsAgreementDTO toMakerTermsAgreementDTO(MakerTermsAgreementRequest request);
}
