package liaison.groble.application.terms.service;

import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;

public interface TermsService {

  boolean getAdvertisingAgreementStatus(Long userId);

  void updateAdvertisingAgreementStatus(
      Long userId, boolean agreed, String ipAddress, String userAgent);

  MakerTermsAgreementDTO agreeMakerTerms(
      Long userId, MakerTermsAgreementDTO dto, String clientIp, String userAgent);
}
