package liaison.groble.api.server.util;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.application.file.dto.FileUploadDTO;

@Component
public class FileUtil {
  public FileUploadDTO toServiceFileUploadDTO(MultipartFile file, String directory)
      throws IOException {
    return FileUploadDTO.builder()
        .inputStream(file.getInputStream())
        .fileName(UUID.randomUUID() + "_" + file.getOriginalFilename())
        .originalFilename(file.getOriginalFilename())
        .contentType(file.getContentType())
        .fileSize(file.getSize())
        .directory(directory)
        .storagePath(directory + "/" + file.getOriginalFilename())
        .build();
  }
}
