package liaison.groble.application.file;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.file.dto.FileDto;
import liaison.groble.application.file.dto.FileUploadDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.FileSizeLimitExceededException;
import liaison.groble.common.exception.InvalidFileTypeException;
import liaison.groble.domain.file.entity.FileInfo;
import liaison.groble.domain.file.entity.PresignedUrlInfo;
import liaison.groble.domain.file.repository.FileRepository;
import liaison.groble.domain.file.service.FileStorageService;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

  private final FileStorageService fileStorageService;
  private final FileRepository fileRepository;
  private final UserRepository userRepository;
  private final UserReader userReader;

  /** 파일을 S3에 업로드하고 DB에 정보를 저장합니다. 디렉토리 타입에 따라 다른 비즈니스 로직을 적용합니다. */
  @Transactional
  public FileDto uploadFile(Long userId, FileUploadDto fileUploadDto) {
    // 업로드 전 디렉토리 타입에 따른 권한 검증
    validateUserPermissionByDirectory(userId, fileUploadDto);

    String originalFileName = fileUploadDto.getOriginalFilename();
    String contentType = fileUploadDto.getContentType();
    String fileName = fileUploadDto.getFileName();
    long fileSize = fileUploadDto.getFileSize();
    String directory = fileUploadDto.getDirectory();

    // S3에 파일 업로드
    String fileUrl =
        fileStorageService.uploadFile(
            fileUploadDto.getInputStream(), fileName, contentType, directory);

    // 파일 정보 DB에 저장
    FileInfo fileInfo =
        FileInfo.builder()
            .fileName(fileName)
            .originalFilename(originalFileName)
            .fileUrl(fileUrl)
            .contentType(contentType)
            .fileSize(fileSize)
            .storagePath(directory + "/" + fileName)
            .userId(userId) // 파일 소유자 ID 저장
            .build();

    FileInfo savedFileInfo = fileRepository.save(fileInfo);

    // 디렉토리 타입에 따른 후처리 로직 실행
    processFileByDirectory(userId, savedFileInfo);

    return FileDto.builder()
        .originalFilename(originalFileName)
        .contentType(contentType)
        .fileUrl(fileUrl)
        .build();
  }

  /** 디렉토리 타입에 따라 사용자 권한을 검증합니다. */
  private void validateUserPermissionByDirectory(Long userId, FileUploadDto fileUploadDto) {
    String directory = fileUploadDto.getDirectory();

    if (directory.equals("profile")) { // 프로필 사진 업로드 권한 검증
      validateProfileUploadPermission(userId, fileUploadDto);
    }
  }

  /** 프로필 사진 업로드 권한을 검증합니다. */
  private void validateProfileUploadPermission(Long userId, FileUploadDto fileUploadDto) {
    // 요청한 사용자가 실제 존재하는지 확인
    User user = userReader.getUserById(userId);

    // 요청 파라미터에 특정 사용자 식별자가 있는 경우 (예: profileUserId)
    //    if (fileUploadDto.getMetadata() != null
    //        && fileUploadDto.getMetadata().containsKey("profileUserId")) {
    //      Long profileUserId = Long.valueOf(fileUploadDto.getMetadata().get("profileUserId"));
    //
    //      // 본인 프로필이 아니고 관리자도 아닌 경우 권한 없음
    //      if (!userId.equals(profileUserId) && !user.hasRole("ROLE_ADMIN")) {
    //        throw new UnauthorizedException("다른 사용자의 프로필 사진을 업로드할 권한이 없습니다.");
    //      }
    //    }

    // 파일 타입 검증 (이미지 파일만 허용)
    if (!fileUploadDto.getContentType().startsWith("image/")) {
      throw new InvalidFileTypeException("프로필 사진은 이미지 파일만 업로드할 수 있습니다.");
    }

    // 파일 크기 제한 (예: 5MB)
    long maxFileSize = 5 * 1024 * 1024; // 5MB
    if (fileUploadDto.getFileSize() > maxFileSize) {
      throw new FileSizeLimitExceededException("프로필 사진은 5MB 이하만 업로드할 수 있습니다.");
    }
  }

  /** 디렉토리 타입에 따른 후처리 로직을 실행합니다. */
  private void processFileByDirectory(Long userId, FileInfo fileInfo) {
    String directory = fileInfo.getStoragePath().split("/")[0];
    if (directory.equals("profiles")) { // 프로필 이미지 업로드 후처리 (기존 프로필 이미지 삭제, 유저 정보 업데이트 등)
      updateUserProfileImage(userId, fileInfo);
    }
  }

  /** 사용자 프로필 이미지를 업데이트합니다. */
  private void updateUserProfileImage(Long userId, FileInfo fileInfo) {
    User user = userReader.getUserById(userId);
    if (user.getProfileImageUrl() != null) {
      FileInfo oldImageInfo = fileRepository.findByFileUrl(user.getProfileImageUrl());
      if (oldImageInfo != null) {
        fileStorageService.deleteFile(oldImageInfo.getStoragePath());
        fileRepository.delete(oldImageInfo);
      }
    }

    user.updateProfileImageUrl(fileInfo.getFileUrl());
    userRepository.save(user);
  }

  /** 상품 이미지를 업데이트합니다. */
  //    private void updateProductImage(Long productId, FileInfo fileInfo) {
  //        Product product = productRepository.findById(productId)
  //                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다. ID: " +
  // productId));
  //
  //        // 메타데이터에 이미지 순서 정보가 있는 경우
  //        String imageOrder = "0"; // 기본값
  //        if (fileInfo.getMetadata() != null && fileInfo.getMetadata().containsKey("imageOrder"))
  // {
  //            imageOrder = fileInfo.getMetadata().get("imageOrder");
  //        }
  //
  //        // 상품 이미지 관계 저장
  //        ProductImage productImage = ProductImage.builder()
  //                .productId(productId)
  //                .fileId(fileInfo.getId())
  //                .imageOrder(Integer.valueOf(imageOrder))
  //                .build();
  //        productImageRepository.save(productImage);
  //
  //        // 대표 이미지(첫 번째 이미지)인 경우 상품 정보 업데이트
  //        if (imageOrder.equals("0")) {
  //            product.setThumbnailUrl(fileInfo.getFileUrl());
  //            productRepository.save(product);
  //        }
  //    }

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
    }
  }
}
