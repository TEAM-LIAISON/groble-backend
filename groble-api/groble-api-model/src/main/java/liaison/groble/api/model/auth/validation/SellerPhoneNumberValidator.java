package liaison.groble.api.model.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.request.SocialSignUpRequest;

public class SellerPhoneNumberValidator
    implements ConstraintValidator<ValidSellerPhoneNumber, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value instanceof SignUpRequest request) {
      return validate(request.getUserType(), request.getPhoneNumber(), context);
    } else if (value instanceof SocialSignUpRequest request) {
      return validate(request.getUserType(), request.getPhoneNumber(), context);
    }
    return true; // 지원하지 않는 타입이면 검증 생략
  }

  private boolean validate(
      String userType, String phoneNumber, ConstraintValidatorContext context) {
    if ("SELLER".equalsIgnoreCase(userType)) {
      if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("SELLER는 전화번호를 반드시 입력해야 합니다.")
            .addPropertyNode("phoneNumber")
            .addConstraintViolation();
        return false;
      }
    }
    return true;
  }
}
