package liaison.groble.api.model.admin.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrderCancelActionValidator
    implements ConstraintValidator<ValidOrderCancelAction, String> {

  private static final Set<String> VALID_ACTIONS = Set.of("approve", "reject");

  @Override
  public void initialize(ValidOrderCancelAction constraintAnnotation) {
    // 초기화 로직이 필요한 경우 구현
  }

  @Override
  public boolean isValid(String action, ConstraintValidatorContext context) {
    if (action == null) {
      return false;
    }
    return VALID_ACTIONS.contains(action.toLowerCase());
  }
}
