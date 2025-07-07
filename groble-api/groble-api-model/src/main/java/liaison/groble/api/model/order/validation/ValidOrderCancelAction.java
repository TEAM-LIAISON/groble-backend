package liaison.groble.api.model.order.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderCancelActionValidator.class)
public @interface ValidOrderCancelAction {
  String message() default "유효하지 않은 취소 요청 액션입니다. 'approve' 또는 'reject'만 가능합니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
