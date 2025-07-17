package liaison.groble.mapping.terms;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.terms.request.MakerTermsAgreementRequest;
import liaison.groble.api.model.terms.request.TermsAgreementRequest;
import liaison.groble.api.model.terms.response.TermsAgreementResponse;
import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;
import liaison.groble.application.terms.dto.TermsAgreementDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-17T20:40:12+0900",
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

  @Override
  public TermsAgreementDTO toTermsAgreementDTO(TermsAgreementRequest request) {
    if (request == null) {
      return null;
    }

    TermsAgreementDTO.TermsAgreementDTOBuilder termsAgreementDTO = TermsAgreementDTO.builder();

    termsAgreementDTO.termsTypeStrings(mapTermsTypesToStrings(request.getTermsTypes()));

    return termsAgreementDTO.build();
  }

  @Override
  public TermsAgreementResponse toTermsAgreementResponse(TermsAgreementDTO dto) {
    if (dto == null) {
      return null;
    }

    TermsAgreementResponse.TermsAgreementResponseBuilder termsAgreementResponse =
        TermsAgreementResponse.builder();

    if (dto.getId() != null) {
      termsAgreementResponse.id(dto.getId());
    }
    if (dto.getTitle() != null) {
      termsAgreementResponse.title(dto.getTitle());
    }
    if (dto.getVersion() != null) {
      termsAgreementResponse.version(dto.getVersion());
    }
    termsAgreementResponse.required(dto.isRequired());
    if (dto.getContentUrl() != null) {
      termsAgreementResponse.contentUrl(dto.getContentUrl());
    }
    termsAgreementResponse.agreed(dto.isAgreed());
    if (dto.getAgreedAt() != null) {
      termsAgreementResponse.agreedAt(dto.getAgreedAt());
    }
    if (dto.getEffectiveFrom() != null) {
      termsAgreementResponse.effectiveFrom(dto.getEffectiveFrom());
    }
    if (dto.getEffectiveTo() != null) {
      termsAgreementResponse.effectiveTo(dto.getEffectiveTo());
    }

    termsAgreementResponse.type(mapStringToTermsType(dto.getTypeString()));

    return termsAgreementResponse.build();
  }
}
