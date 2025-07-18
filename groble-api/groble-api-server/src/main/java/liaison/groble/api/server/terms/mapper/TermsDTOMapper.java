package liaison.groble.api.server.terms.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.terms.enums.TermsTypeDTO;
import liaison.groble.api.model.terms.response.TermsAgreementResponse;
import liaison.groble.application.terms.dto.TermsAgreementDTO;

@Component
public class TermsDTOMapper {

  public TermsAgreementResponse toApiTermsAgreementResponse(TermsAgreementDTO termsAgreementDTO) {
    TermsTypeDTO termsTypeDTO =
        termsAgreementDTO.getTypeString() != null
            ? TermsTypeDTO.valueOf(termsAgreementDTO.getTypeString())
            : null;

    return TermsAgreementResponse.builder()
        .id(termsAgreementDTO.getId())
        .type(termsTypeDTO)
        .title(termsAgreementDTO.getTitle())
        .version(termsAgreementDTO.getVersion())
        .required(termsAgreementDTO.isRequired())
        .contentUrl(termsAgreementDTO.getContentUrl())
        .agreed(termsAgreementDTO.isAgreed())
        .agreedAt(termsAgreementDTO.getAgreedAt())
        .effectiveFrom(termsAgreementDTO.getEffectiveFrom())
        .effectiveTo(termsAgreementDTO.getEffectiveTo())
        .build();
  }

  public List<TermsAgreementResponse> toApiTermsAgreementResponseList(
      List<TermsAgreementDTO> termsAgreementDTOs) {
    return termsAgreementDTOs.stream()
        .map(this::toApiTermsAgreementResponse)
        .collect(Collectors.toList());
  }

  public TermsAgreementDTO toServiceOrderTermsAgreementDTO() {
    List<String> termTypeStrs =
        List.of("ELECTRONIC_FINANCIAL", "PURCHASE_POLICY", "PERSONAL_INFORMATION");

    return TermsAgreementDTO.builder()
        .termsTypeStrings(termTypeStrs) // 문자열 리스트로 전달
        .build();
  }
}
