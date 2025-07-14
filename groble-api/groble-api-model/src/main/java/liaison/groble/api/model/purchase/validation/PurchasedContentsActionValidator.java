package liaison.groble.api.model.purchase.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PurchasedContentsActionValidator
    implements ConstraintValidator<ValidPurchasedContentsAction, String> {
  private static final Set<String> VALID_ACTIONS = Set.of("PAID", "CANCEL");

  @Override
  public boolean isValid(String action, ConstraintValidatorContext context) {
    if (action == null) {
      return false;
    }
    return VALID_ACTIONS.contains(action.toLowerCase());
  }
}
