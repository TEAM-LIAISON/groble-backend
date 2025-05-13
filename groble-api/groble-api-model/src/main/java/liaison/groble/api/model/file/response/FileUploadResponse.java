package liaison.groble.api.model.file.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
  @Schema(description = "원본 파일 이름", example = "thumbnail.jpg")
  private String originalFileName;

  @Schema(description = "파일 URL", example = "https://storage.example.com/contents/thumbnail.jpg")
  private String fileUrl;

  @Schema(description = "파일 MIME 타입", example = "image/jpeg")
  private String contentType;

  @Schema(description = "파일 저장 경로", example = "content/thumbnail")
  private String directory;

  public static FileUploadResponse of(
      String originalFileName, String fileUrl, String contentType, String directory) {
    return FileUploadResponse.builder()
        .originalFileName(originalFileName)
        .fileUrl(fileUrl)
        .contentType(contentType)
        .directory(directory)
        .build();
  }
}
