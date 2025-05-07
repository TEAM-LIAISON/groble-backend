package liaison.groble.api.model.file.response.swagger;

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

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "단건 파일 업로드",
    description =
        "폼 데이터를 통해 다양한 유형의 파일을 업로드합니다. fileType 파라미터를 통해 파일 저장 위치를 자동으로 결정하거나 directory 파라미터로 직접 지정할 수 있습니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "201",
      description = "파일 업로드 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = FileUploadApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "파일 업로드 성공 응답",
                    summary = "파일 업로드 성공",
                    value =
                        """
                                    {
                                      "status": "SUCCESS",
                                      "code": 201,
                                      "message": "파일 업로드가 성공적으로 완료되었습니다.",
                                      "data": {
                                        "originalFileName": "sample-document.pdf",
                                        "fileUrl": "https://storage.example.com/contents/sample-document.pdf",
                                        "contentType": "application/pdf",
                                        "directory": "contents"
                                      },
                                      "timestamp": "2025-05-06 04:26:26"
                                    }
                                    """)
              })),
  @ApiResponse(responseCode = "400", description = "잘못된 요청 - 파일이 없거나 손상된 경우"),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "413", description = "파일 크기 제한 초과"),
  @ApiResponse(responseCode = "500", description = "서버 오류 - 파일 저장 실패")
})
public @interface UploadFile {}
