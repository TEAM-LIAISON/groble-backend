package liaison.groble.application.terms.service;

import java.util.List;

import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;
import liaison.groble.application.terms.dto.TermsAgreementDTO;

public interface TermsService {

  TermsAgreementDTO agreeToTerms(TermsAgreementDTO dto);

  TermsAgreementDTO withdrawTermsAgreement(TermsAgreementDTO dto);

  List<TermsAgreementDTO> getUserTermsAgreements(Long userId);

  List<TermsAgreementDTO> getActiveTerms();

  boolean getAdvertisingAgreementStatus(Long userId);

  void updateAdvertisingAgreementStatus(
      Long userId, boolean agreed, String ipAddress, String userAgent);

  MakerTermsAgreementDTO agreeMakerTerms(
      Long userId, MakerTermsAgreementDTO dto, String clientIp, String userAgent);
}
