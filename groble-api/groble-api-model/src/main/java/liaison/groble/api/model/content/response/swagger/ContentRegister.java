package liaison.groble.api.model.content.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "콘텐츠 심사 요청에 대한 응답", description = "콘텐츠 심사 요청 이후 해당 정보를 조회합니다.")
public @interface ContentRegister {}
