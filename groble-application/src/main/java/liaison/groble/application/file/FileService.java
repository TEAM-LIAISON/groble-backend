package liaison.groble.application.file;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.domain.file.entity.FileInfo;
import liaison.groble.domain.file.entity.PresignedUrlInfo;
import liaison.groble.domain.file.repository.FileRepository;
import liaison.groble.domain.file.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

  private final FileStorageService fileStorageService;
  private final FileRepository fileRepository;

  /** 파일을 S3에 업로드하고 DB에 정보를 저장합니다. */
  @Transactional
  public FileInfo uploadFile(MultipartFile file, String directory) {
    try {
      String originalFileName = file.getOriginalFilename();
      String contentType = file.getContentType();
      String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
      long fileSize = file.getSize();

      // S3에 파일 업로드
      String fileUrl =
          fileStorageService.uploadFile(file.getInputStream(), fileName, contentType, directory);

      // 파일 정보 DB에 저장
      FileInfo fileInfo =
          FileInfo.builder()
              .fileName(fileName)
              .originalFilename(originalFileName)
              .fileUrl(fileUrl)
              .contentType(contentType)
              .fileSize(fileSize)
              .storagePath(directory + "/" + fileName)
              .build();

      return fileRepository.save(fileInfo);
    } catch (IOException e) {
      log.error("파일 업로드 중 오류 발생", e);
      throw new RuntimeException("파일 업로드에 실패했습니다", e);
    }
  }

  /** 클라이언트가 직접 S3에 업로드할 수 있는 Presigned URL을 생성합니다. */
  public PresignedUrlInfo generatePresignedUrl(
      String originalFileName, String contentType, String directory) {
    String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
    return fileStorageService.generatePresignedUrl(fileName, contentType, directory);
  }

  /** Presigned URL을 통해 업로드된 파일 정보를 DB에 저장합니다. */
  @Transactional
  public FileInfo saveFileInfo(
      String fileName,
      String originalFileName,
      String fileUrl,
      String contentType,
      long fileSize,
      String storagePath) {
    FileInfo fileInfo =
        FileInfo.builder()
            .fileName(fileName)
            .originalFilename(originalFileName)
            .fileUrl(fileUrl)
            .contentType(contentType)
            .fileSize(fileSize)
            .storagePath(storagePath)
            .build();

    return fileRepository.save(fileInfo);
  }

  /** 파일을 삭제합니다. */
  @Transactional
  public void deleteFile(String fileName) {
    FileInfo fileInfo = fileRepository.findByFileName(fileName);
    if (fileInfo != null) {
      fileStorageService.deleteFile(fileInfo.getStoragePath());
      // DB에서 파일 정보 삭제 로직 추가
    }
  }
}
