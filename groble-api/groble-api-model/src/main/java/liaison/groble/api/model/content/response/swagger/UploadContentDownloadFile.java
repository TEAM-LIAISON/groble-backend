package liaison.groble.api.model.content.response.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import liaison.groble.api.model.file.response.swagger.FileUploadApiResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "자료 콘텐츠 즉시 다운로드 선택 시 파일 업로드",
    description =
        "자료 콘텐츠 즉시 다운로드 선택 시 파일을 업로드합니다. pdf, zip 파일만 업로드 가능하며, 다른 파일 형식은 오류가 발생합니다. "
            + "반환된 fileUrl을 콘텐츠 임시 저장 및 심사 요청에 포함하여 사용합니다.")
@ApiResponses({
  // --- 성공 201 Created ---
  @ApiResponse(
      responseCode = "201",
      description = "파일 업로드 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = FileUploadApiResponse.class),
              examples =
                  @ExampleObject(
                      name = "성공 예시",
                      summary = "201 Created",
                      value =
                          """
                        {
                          "status":  "SUCCESS",
                          "code":    201,
                          "message": "자료 콘텐츠 파일 업로드가 성공적으로 완료되었습니다.",
                          "data": {
                            "originalFileName": "document.pdf",
                            "fileUrl":          "https://storage.example.com/contents/document/document.pdf",
                            "contentType":      "application/pdf",
                            "directory":        "/contents/document"
                          },
                          "timestamp": "2025-05-06 04:26:26"
                        }
                        """))),
  // --- 400 Bad Request ---
  @ApiResponse(
      responseCode = "400",
      description = "잘못된 요청 - 파일이 없거나 pdf/zip 파일이 아닌 경우",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GrobleResponse.class),
              examples =
                  @ExampleObject(
                      name = "Bad Request 예시",
                      summary = "400 Bad Request",
                      value =
                          """
                        {
                          "status": "ERROR",
                          "code":   400,
                          "message":"pdf/zip 파일만 업로드 가능합니다.",
                          "timestamp":"2025-05-15 14:32:00"
                        }
                        """))),
  // --- 401 Unauthorized ---
  @ApiResponse(
      responseCode = "401",
      description = "인증 실패 (AccessToken 만료 또는 없음)",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GrobleResponse.class),
              examples =
                  @ExampleObject(
                      name = "Unauthorized 예시",
                      summary = "401 Unauthorized",
                      value =
                          """
                        {
                          "status": "ERROR",
                          "code":   401,
                          "message":"인증이 필요합니다. 유효한 AccessToken을 헤더에 포함하세요.",
                          "timestamp":"2025-05-15 14:32:00"
                        }
                        """))),
  // --- 413 Payload Too Large ---
  @ApiResponse(
      responseCode = "413",
      description = "파일 크기 제한 초과",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GrobleResponse.class),
              examples =
                  @ExampleObject(
                      name = "Payload Too Large 예시",
                      summary = "413 Payload Too Large",
                      value =
                          """
                        {
                          "status": "ERROR",
                          "code":   413,
                          "message":"파일 용량이 최대 허용 크기를 초과했습니다.",
                          "timestamp":"2025-05-15 14:32:00"
                        }
                        """))),
  // --- 500 Internal Server Error ---
  @ApiResponse(
      responseCode = "500",
      description = "서버 오류 - 파일 저장 실패",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GrobleResponse.class),
              examples =
                  @ExampleObject(
                      name = "Internal Server Error 예시",
                      summary = "500 Internal Server Error",
                      value =
                          """
                        {
                          "status": "ERROR",
                          "code":   500,
                          "message":"파일 객체 저장 중 오류가 발생했습니다. 다시 시도해주세요.",
                          "timestamp":"2025-05-15 14:32:00"
                        }
                        """)))
})
public @interface UploadContentDownloadFile {}
