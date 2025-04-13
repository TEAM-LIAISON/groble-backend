package liaison.groble.application.terms.service;

import java.util.List;

import liaison.groble.application.terms.dto.TermsAgreementDto;

public interface TermsService {

  public TermsAgreementDto agreeToTerms(TermsAgreementDto dto);

  public TermsAgreementDto withdrawTermsAgreement(TermsAgreementDto dto);

  public List<TermsAgreementDto> getUserTermsAgreements(Long userId);

  public List<TermsAgreementDto> getActiveTerms();
}
