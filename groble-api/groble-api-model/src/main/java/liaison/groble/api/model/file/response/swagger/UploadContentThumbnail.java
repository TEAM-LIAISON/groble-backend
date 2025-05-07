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
    summary = "콘텐츠 대표(썸네일) 이미지 업로드",
    description =
        "콘텐츠 대표(썸네일) 이미지를 업로드합니다. 이미지 파일만 업로드 가능하며, 다른 파일 형식은 오류가 발생합니다."
            + "반환된 fileUrl을 콘텐츠 임시 저장 및 심사 요청 request에 포함하여 사용합니다.")
@ApiResponses({
  @ApiResponse(
      responseCode = "201",
      description = "이미지 업로드 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = FileUploadApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "이미지 업로드 성공 응답",
                    summary = "콘텐츠 대표(썸네일) 이미지 업로드 성공",
                    value =
                        """
                                    {
                                      "status": "SUCCESS",
                                      "code": 201,
                                      "message": "파일 업로드가 성공적으로 완료되었습니다.",
                                      "data": {
                                        "originalFileName": "thumbnail.jpg",
                                        "fileUrl": "https://storage.example.com/contents/thumbnail.jpg",
                                        "contentType": "image/jpeg",
                                        "directory": "content/thumbnail"
                                      },
                                      "timestamp": "2025-05-06 04:26:26"
                                    }
                                    """)
              })),
  @ApiResponse(responseCode = "400", description = "잘못된 요청 - 파일이 없거나 이미지 파일이 아닌 경우"),
  @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
  @ApiResponse(responseCode = "413", description = "파일 크기 제한 초과"),
  @ApiResponse(responseCode = "500", description = "서버 오류 - 파일 저장 실패")
})
public @interface UploadContentThumbnail {}
