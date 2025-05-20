package liaison.groble.domain.user.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import liaison.groble.domain.terms.Terms;
import liaison.groble.domain.terms.UserTerms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.entity.User;

public class UserTermsService {
  public void agreeToTerms(User user, Terms terms, String agreedIp, String agreedUserAgent) {
    UserTerms agreement =
        UserTerms.builder()
            .user(user)
            .terms(terms)
            .agreed(true)
            .agreedIp(agreedIp)
            .agreedUserAgent(agreedUserAgent)
            .build();

    user.getTermsAgreements().add(agreement);
  }

  public boolean hasAgreedTo(User user, TermsType termsType) {
    return user.getTermsAgreements().stream()
        .anyMatch(
            agreement ->
                agreement.getTerms().getType() == termsType
                    && agreement.isAgreed()
                    && agreement.getTerms().getEffectiveTo() == null);
  }

  public boolean hasAgreedToAllRequiredTerms(User user) {
    return Arrays.stream(TermsType.values())
        .filter(TermsType::isRequired)
        .allMatch(type -> hasAgreedTo(user, type));
  }

  public List<TermsType> getMissingRequiredTerms(User user) {
    return Arrays.stream(TermsType.values())
        .filter(TermsType::isRequired)
        .filter(type -> !hasAgreedTo(user, type))
        .collect(Collectors.toList());
  }

  public boolean hasAgreedToAdvertising(User user) {
    return hasAgreedTo(user, TermsType.ADVERTISING_POLICY);
  }

  public void updateAdvertisingAgreement(
      User user, Terms advertisingTerms, boolean agreed, String ip, String userAgent) {

    UserTerms existingAgreement =
        user.getTermsAgreements().stream()
            .filter(a -> a.getTerms().equals(advertisingTerms))
            .findFirst()
            .orElse(null);

    if (existingAgreement != null) {
      existingAgreement.updateAgreement(agreed, Instant.now(), ip, userAgent);
    } else {
      UserTerms newAgreement =
          UserTerms.builder()
              .user(user)
              .terms(advertisingTerms)
              .agreed(agreed)
              .agreedAt(Instant.now())
              .agreedIp(ip)
              .agreedUserAgent(userAgent)
              .build();

      user.getTermsAgreements().add(newAgreement);
    }
  }
}
