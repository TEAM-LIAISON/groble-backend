package liaison.groble.api.server.file.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.application.file.dto.FileUploadDto;

@Component
public class FileDtoMapper {

  public FileUploadDto toServiceFileUploadDto(MultipartFile file, String directory) {
    return FileUploadDto.builder()
        .fileName(UUID.randomUUID().toString() + "_" + file.getOriginalFilename())
        .originalFilename(file.getOriginalFilename())
        .contentType(file.getContentType())
        .fileSize(file.getSize())
        .directory(directory)
        .storagePath(directory + "/" + file.getOriginalFilename())
        .build();
  }
}
