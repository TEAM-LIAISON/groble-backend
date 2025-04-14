package liaison.groble.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {
  String item() default "";

  String action() default "";

  boolean includeResult() default false;

  boolean includeParam() default false;
}
