package liaison.groble.api.model.auth.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "개인 및 법인 계좌 인증 요청", description = "개인 및 법인 계좌 인증 요청을 진행합니다.")
@RequestBody(
    description = "개인 및 법인 계좌 인증 요청 바디",
    required = true,
    content =
        @Content(
            mediaType = "application/json",
            schema =
                @Schema(
                    implementation =
                        liaison.groble.api.model.auth.request
                            .VerificationBusinessMakerAccountRequest.class),
            examples =
                @ExampleObject(
                    name = "예시 요청",
                    summary = "정상적인 개인 및 법인 계좌 인증 요청 예시",
                    value =
                        """
                {
                  "bankAccountOwner": "홍길동",
                  "bankName": "신한은행",
                  "bankAccountNumber": "110123456789",
                  "copyOfBankbookUrl": "https://image.dev.groble.im/bankbook/honggildong.png"
                }
            """)))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessMakerApiResponse.class))),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
})
public @interface BusinessMaker {}
