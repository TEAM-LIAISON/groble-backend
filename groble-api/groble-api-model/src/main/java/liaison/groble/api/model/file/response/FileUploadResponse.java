package liaison.groble.api.model.file.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
  private String originalFileName;
  private String fileUrl;
  private String contentType;
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
