package liaison.groble.api.server.file.mapper;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.application.file.dto.FileUploadDto;

@Component
public class FileDtoMapper {

  public FileUploadDto toServiceFileUploadDto(MultipartFile file, String directory)
      throws IOException {
    return FileUploadDto.builder()
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
