package liaison.groble.api.server.terms.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.terms.enums.TermsTypeDTO;
import liaison.groble.api.model.terms.request.TermsAgreementRequest;
import liaison.groble.api.model.terms.response.TermsAgreementResponse;
import liaison.groble.application.terms.dto.TermsAgreementDTO;

@Component
public class TermsDtoMapper {

  public TermsAgreementDTO toServiceTermsAgreementDTO(TermsAgreementRequest request) {
    // API 계층의 TermsTypeDto를 문자열로 변환
    List<String> termTypeStrs =
        request.getTermsTypes().stream().map(Enum::name).collect(Collectors.toList());

    return TermsAgreementDTO.builder()
        .termsTypeStrings(termTypeStrs) // 문자열 리스트로 전달
        .build();
  }

  public TermsAgreementResponse toApiTermsAgreementResponse(TermsAgreementDTO dto) {
    // 서비스 계층에서 전달된 문자열 type을 TermsTypeDto로 변환
    TermsTypeDTO typeDto =
        dto.getTypeString() != null ? TermsTypeDTO.valueOf(dto.getTypeString()) : null;

    return TermsAgreementResponse.builder()
        .id(dto.getId())
        .type(typeDto)
        .title(dto.getTitle())
        .version(dto.getVersion())
        .required(dto.isRequired())
        .contentUrl(dto.getContentUrl())
        .agreed(dto.isAgreed())
        .agreedAt(dto.getAgreedAt())
        .effectiveFrom(dto.getEffectiveFrom())
        .effectiveTo(dto.getEffectiveTo())
        .build();
  }

  public List<TermsAgreementResponse> toApiTermsAgreementResponseList(
      List<TermsAgreementDTO> dtos) {
    return dtos.stream().map(this::toApiTermsAgreementResponse).collect(Collectors.toList());
  }

  public TermsAgreementDTO toServiceOrderTermsAgreementDto() {
    List<String> termTypeStrs =
        List.of("ELECTRONIC_FINANCIAL", "PURCHASE_POLICY", "PERSONAL_INFORMATION");

    return TermsAgreementDTO.builder()
        .termsTypeStrings(termTypeStrs) // 문자열 리스트로 전달
        .build();
  }
}
