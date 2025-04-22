package liaison.groble.application.file.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDto {
  private String originalFilename;
  private String contentType;
  private String fileUrl;
}
