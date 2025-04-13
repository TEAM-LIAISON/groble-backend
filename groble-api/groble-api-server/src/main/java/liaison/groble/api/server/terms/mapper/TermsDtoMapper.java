package liaison.groble.api.server.terms.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.terms.enums.TermsTypeDto;
import liaison.groble.api.model.terms.request.TermsAgreementRequest;
import liaison.groble.api.model.terms.response.TermsAgreementResponse;
import liaison.groble.application.terms.dto.TermsAgreementDto;

@Component
public class TermsDtoMapper {

  public TermsAgreementDto toServiceTermsAgreementDto(TermsAgreementRequest request) {
    // API 계층의 TermsTypeDto를 문자열로 변환
    List<String> termTypeStrs =
        request.getTermsTypes().stream().map(Enum::name).collect(Collectors.toList());

    return TermsAgreementDto.builder()
        .termsTypeStrings(termTypeStrs) // 문자열 리스트로 전달
        .build();
  }

  public TermsAgreementResponse toApiTermsAgreementResponse(TermsAgreementDto dto) {
    // 서비스 계층에서 전달된 문자열 type을 TermsTypeDto로 변환
    TermsTypeDto typeDto =
        dto.getTypeString() != null ? TermsTypeDto.valueOf(dto.getTypeString()) : null;

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
      List<TermsAgreementDto> dtos) {
    return dtos.stream().map(this::toApiTermsAgreementResponse).collect(Collectors.toList());
  }
}
