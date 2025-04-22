package liaison.groble.application.file.dto;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDto {
  private InputStream inputStream;
  private String fileName;
  private String originalFilename;
  private String contentType;
  private long fileSize;
  private String directory;
  private String storagePath;
}
