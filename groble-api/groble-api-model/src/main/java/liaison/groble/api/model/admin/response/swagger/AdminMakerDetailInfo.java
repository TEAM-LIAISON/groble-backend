package liaison.groble.api.model.admin.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "[✅ 관리자 메이커] 메이커 상세 정보 조회")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = AdminMakerDetailInfoApiResponse.class),
              examples =
                  @ExampleObject(
                      name = "success",
                      summary = "표준 성공 응답",
                      value =
                          """
                {
                  "status": "SUCCESS",
                  "code": 200,
                  "message": "요청이 성공적으로 처리되었습니다.",
                  "data": {
                    "isBusinessMaker": true,
                    "bankAccountOwner": "홍길동",
                    "bankName": "카카오뱅크",
                    "bankAccountNumber": "3333-02-1234567",
                    "copyOfBankbookUrl": "https://cdn.example.com/bankbook/abc123.jpg",
                    "businessType": "individual-simple",
                    "businessCategory": "IT 서비스업",
                    "businessSector": "서비스",
                    "businessName": "링킷",
                    "representativeName": "김철수",
                    "businessAddress": "서울특별시 강남구 테헤란로 123 4F",
                    "businessLicenseFileUrl": "https://cdn.example.com/license/xyz789.pdf",
                    "taxInvoiceEmail": "tax@example.com"
                  },
                  "timestamp": "2025-06-12 18:30:00"
                }
              """)))
})
public @interface AdminMakerDetailInfo {}
