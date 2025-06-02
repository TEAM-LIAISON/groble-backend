package liaison.groble.api.model.user.response.swagger;

import java.lang.annotation.*;

import liaison.groble.api.model.file.response.swagger.FileUploadApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "사용자 프로필 이미지 업로드",
    description = "사용자 프로필 이미지를 업로드합니다. 이미지 파일만 업로드 가능하며, 다른 파일 형식은 오류가 발생합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "201",
      description = "프로필 이미지 업로드 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = FileUploadApiResponse.class),
              examples =
                  @ExampleObject(
                      name = "프로필 업로드 성공",
                      summary = "201 Created",
                      value =
                          """
                                        {
                                          "status":  "SUCCESS",
                                          "code":    201,
                                          "message": "프로필 이미지가 성공적으로 업로드되었습니다.",
                                          "data": {
                                            "originalFileName": "profile.png",
                                            "fileUrl":          "https://cdn.example.com/profiles/123/profile.png",
                                            "contentType":      "image/png",
                                            "directory":        "/profiles/123"
                                          },
                                          "timestamp": "2025-05-15 15:00:00"
                                        }
                                        """))),
  @ApiResponse(
      responseCode = "400",
      description = "잘못된 요청 - 파일이 없거나 이미지 파일이 아닌 경우",
      content =
          @Content(
              mediaType = "application/json",
              schema =
                  @Schema(implementation = liaison.groble.common.response.GrobleResponse.class),
              examples =
                  @ExampleObject(
                      name = "Bad Request 예시",
                      summary = "400 Bad Request",
                      value =
                          """
                                        {
                                          "status":  "ERROR",
                                          "code":    400,
                                          "message": "이미지 파일을 선택해주세요.",
                                          "timestamp": "2025-05-15 15:00:00"
                                        }
                                        """))),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(
      responseCode = "500",
      description = "서버 오류 - 파일 저장 실패",
      content =
          @Content(
              mediaType = "application/json",
              schema =
                  @Schema(implementation = liaison.groble.common.response.GrobleResponse.class),
              examples =
                  @ExampleObject(
                      name = "Internal Server Error 예시",
                      summary = "500 Internal Server Error",
                      value =
                          """
                                        {
                                          "status":  "ERROR",
                                          "code":    500,
                                          "message": "프로필 이미지 저장 중 오류가 발생했습니다. 다시 시도해주세요.",
                                          "timestamp": "2025-05-15 15:00:00"
                                        }
                                        """)))
})
public @interface UploadUserProfileImage {}
