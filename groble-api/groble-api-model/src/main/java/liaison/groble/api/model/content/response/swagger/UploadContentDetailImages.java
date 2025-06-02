package liaison.groble.api.model.content.response.swagger;

import java.lang.annotation.*;

import liaison.groble.api.model.file.response.swagger.MultiFileUploadApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "콘텐츠 상세 이미지 업로드",
    description = "에디터용 콘텐츠 상세 이미지를 여러 개 업로드합니다. 이미지 파일만 허용하며, 반환된 URL 리스트를 에디터에 삽입하여 사용합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "201",
      description = "상세 이미지 업로드 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = MultiFileUploadApiResponse.class),
              examples =
                  @ExampleObject(
                      name = "상세 이미지 업로드 성공",
                      summary = "201 Created",
                      value =
                          """
                                        {
                                          "status":  "SUCCESS",
                                          "code":    201,
                                          "message": "상세 이미지 업로드가 성공적으로 완료되었습니다.",
                                          "data": [
                                            {
                                              "originalFileName": "detail1.png",
                                              "fileUrl":          "https://cdn.example.com/content/detail/detail1.png",
                                              "contentType":      "image/png",
                                              "directory":        "/contents/detail"
                                            },
                                            {
                                              "originalFileName": "detail2.jpg",
                                              "fileUrl":          "https://cdn.example.com/content/detail/detail2.jpg",
                                              "contentType":      "image/jpeg",
                                              "directory":        "/contents/detail"
                                            }
                                          ],
                                          "timestamp": "2025-05-15 14:32:00"
                                        }
                                        """))),
  @ApiResponse(
      responseCode = "400",
      description = "잘못된 요청 - 파일이 없거나 이미지 파일이 아닌 경우",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = MultiFileUploadApiResponse.class),
              examples =
                  @ExampleObject(
                      name = "Bad Request 예시",
                      summary = "400 Bad Request",
                      value =
                          """
                                        {
                                          "status": "ERROR",
                                          "code":   400,
                                          "message":"이미지 파일만 업로드 가능합니다.",
                                          "timestamp":"2025-05-15 14:32:00"
                                        }
                                        """))),
  @ApiResponse(responseCode = "401", description = "인증 실패"),
  @ApiResponse(responseCode = "413", description = "파일 크기 제한 초과"),
  @ApiResponse(responseCode = "500", description = "서버 오류 - 파일 저장 실패")
})
public @interface UploadContentDetailImages {}
