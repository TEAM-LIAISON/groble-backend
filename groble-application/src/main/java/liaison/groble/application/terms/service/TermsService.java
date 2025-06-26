package liaison.groble.application.terms.service;

import java.util.List;

import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;
import liaison.groble.application.terms.dto.TermsAgreementDto;

public interface TermsService {

  TermsAgreementDto agreeToTerms(TermsAgreementDto dto);

  TermsAgreementDto withdrawTermsAgreement(TermsAgreementDto dto);

  List<TermsAgreementDto> getUserTermsAgreements(Long userId);

  List<TermsAgreementDto> getActiveTerms();

  boolean getAdvertisingAgreementStatus(Long userId);

  void updateAdvertisingAgreementStatus(
      Long userId, boolean agreed, String ipAddress, String userAgent);

  MakerTermsAgreementDTO agreeMakerTerms(
      Long userId, MakerTermsAgreementDTO dto, String clientIp, String userAgent);
}
