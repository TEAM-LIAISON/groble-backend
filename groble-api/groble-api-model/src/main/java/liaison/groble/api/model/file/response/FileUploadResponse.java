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
  private String contentType;
  private String originalFileName;
  private String directory;
}
