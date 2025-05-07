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
    summary = "여러 콘텐츠 파일 업로드",
    description = "즉시 다운로드에 대한 여러 콘텐츠 파일을 한 번에 업로드합니다. 비어있지 않은 파일만 처리합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "201",
      description = "여러 파일 업로드 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = MultipleFilesUploadApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "여러 파일 업로드 성공 응답",
                    summary = "여러 파일 업로드 성공",
                    value =
                        """
                                    {
                                      "status": "SUCCESS",
                                      "code": 201,
                                      "message": "파일 업로드가 성공적으로 완료되었습니다.",
                                      "data": [
                                        {
                                          "originalFileName": "document1.pdf",
                                          "fileUrl": "https://storage.example.com/contents/document1.pdf",
                                          "contentType": "application/pdf",
                                          "directory": "contents"
                                        },
                                        {
                                          "originalFileName": "document2.docx",
                                          "fileUrl": "https://storage.example.com/contents/document2.docx",
                                          "contentType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                          "directory": "contents"
                                        }
                                      ],
                                      "timestamp": "2025-05-06 04:26:26"
                                    }
                                    """)
              })),
  @ApiResponse(responseCode = "400", description = "잘못된 요청 - 파일이 없거나 모든 파일이 비어있는 경우"),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "413", description = "파일 크기 제한 초과"),
  @ApiResponse(responseCode = "500", description = "서버 오류 - 파일 저장 실패")
})
public @interface UploadMultipleFiles {}
