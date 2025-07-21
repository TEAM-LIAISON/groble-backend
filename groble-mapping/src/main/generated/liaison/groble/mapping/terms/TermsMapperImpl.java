package liaison.groble.mapping.terms;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.terms.request.MakerTermsAgreementRequest;
import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-19T17:17:01+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class TermsMapperImpl implements TermsMapper {

  @Override
  public MakerTermsAgreementDTO toMakerTermsAgreementDTO(MakerTermsAgreementRequest request) {
    if (request == null) {
      return null;
    }

    MakerTermsAgreementDTO.MakerTermsAgreementDTOBuilder makerTermsAgreementDTO =
        MakerTermsAgreementDTO.builder();

    if (request.getMakerTermsAgreement() != null) {
      makerTermsAgreementDTO.makerTermsAgreement(request.getMakerTermsAgreement());
    }

    return makerTermsAgreementDTO.build();
  }
}
