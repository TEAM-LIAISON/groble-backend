package liaison.groble.api.server.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 검증 유틸리티 클래스
 *
 * <p>MultipartFile에 대한 중앙화된 검증 로직을 제공합니다. 브라우저 호환성을 고려한 Content-Type과 파일 확장자 검증을 수행합니다.
 */
@Slf4j
@Component
public class FileValidationUtil {

  // Content-Type 상수들
  private static final String APPLICATION_PDF = "application/pdf";
  private static final String APPLICATION_ZIP = "application/zip";
  private static final String APPLICATION_X_ZIP_COMPRESSED = "application/x-zip-compressed";
  private static final String APPLICATION_X_ZIP = "application/x-zip";
  private static final String APPLICATION_ZIP_COMPRESSED = "application/zip-compressed";
  private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

  private static final String IMAGE_JPEG = "image/jpeg";
  private static final String IMAGE_JPG = "image/jpg";
  private static final String IMAGE_PJPEG = "image/pjpeg";
  private static final String IMAGE_PNG = "image/png";
  private static final String IMAGE_X_PNG = "image/x-png";

  // 파일 타입별 허용 Content-Type 집합
  private static final Set<String> PDF_CONTENT_TYPES =
      Set.of(APPLICATION_PDF, APPLICATION_OCTET_STREAM);

  private static final Set<String> ZIP_CONTENT_TYPES =
      Set.of(
          APPLICATION_ZIP,
          APPLICATION_X_ZIP_COMPRESSED,
          APPLICATION_X_ZIP,
          APPLICATION_ZIP_COMPRESSED,
          APPLICATION_OCTET_STREAM);

  private static final Set<String> JPEG_CONTENT_TYPES =
      Set.of(IMAGE_JPEG, IMAGE_JPG, IMAGE_PJPEG, APPLICATION_OCTET_STREAM);

  private static final Set<String> PNG_CONTENT_TYPES =
      Set.of(IMAGE_PNG, IMAGE_X_PNG, APPLICATION_OCTET_STREAM);

  private static final Set<String> IMAGE_CONTENT_TYPES = new HashSet<>();

  static {
    IMAGE_CONTENT_TYPES.addAll(JPEG_CONTENT_TYPES);
    IMAGE_CONTENT_TYPES.addAll(PNG_CONTENT_TYPES);
    IMAGE_CONTENT_TYPES.add("image/gif");
    IMAGE_CONTENT_TYPES.add("image/bmp");
    IMAGE_CONTENT_TYPES.add("image/webp");
  }

  /**
   * 파일이 비어있거나 null인지 확인
   *
   * @param file 검증할 파일
   * @return 비어있거나 null인 경우 true
   */
  public boolean isEmpty(MultipartFile file) {
    return file == null || file.isEmpty();
  }

  /**
   * 파일명이 유효한지 확인
   *
   * @param file 검증할 파일
   * @return 유효한 파일명을 가진 경우 true
   */
  public boolean hasValidFileName(MultipartFile file) {
    String fileName = file.getOriginalFilename();
    return fileName != null && !fileName.trim().isEmpty();
  }

  /**
   * 이미지 파일 여부 확인 (간단 버전)
   *
   * @param file 검증할 파일
   * @return 이미지 파일인 경우 true
   */
  public boolean isImageFile(MultipartFile file) {
    if (!hasValidFileName(file)) {
      return false;
    }

    String contentType = file.getContentType();
    if (contentType == null) {
      return false;
    }

    // Content-Type 기반 간단 검증
    return contentType.toLowerCase().startsWith("image/");
  }

  /**
   * 이미지 파일 여부 확인 (엄격 버전)
   *
   * @param file 검증할 파일
   * @return 허용된 이미지 파일인 경우 true
   */
  public boolean isValidImageFile(MultipartFile file) {
    if (!hasValidFileName(file)) {
      return false;
    }

    String fileName = file.getOriginalFilename().toLowerCase();
    String contentType = file.getContentType();

    if (contentType == null) {
      return false;
    }

    // JPEG 파일 검증
    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
      return JPEG_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    // PNG 파일 검증
    if (fileName.endsWith(".png")) {
      return PNG_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    return false;
  }

  /**
   * PDF 파일 여부 확인
   *
   * @param file 검증할 파일
   * @return PDF 파일인 경우 true
   */
  public boolean isPdfFile(MultipartFile file) {
    if (!hasValidFileName(file)) {
      return false;
    }

    String fileName = file.getOriginalFilename().toLowerCase();
    String contentType = file.getContentType();

    if (contentType == null || !fileName.endsWith(".pdf")) {
      return false;
    }

    return PDF_CONTENT_TYPES.contains(contentType.toLowerCase());
  }

  /**
   * ZIP 파일 여부 확인
   *
   * @param file 검증할 파일
   * @return ZIP 파일인 경우 true
   */
  public boolean isZipFile(MultipartFile file) {
    if (!hasValidFileName(file)) {
      return false;
    }

    String fileName = file.getOriginalFilename().toLowerCase();
    String contentType = file.getContentType();

    if (contentType == null || !fileName.endsWith(".zip")) {
      return false;
    }

    return ZIP_CONTENT_TYPES.contains(contentType.toLowerCase());
  }

  /**
   * PDF 또는 ZIP 파일 여부 확인
   *
   * @param file 검증할 파일
   * @return PDF 또는 ZIP 파일인 경우 true
   */
  public boolean isPdfOrZipFile(MultipartFile file) {
    return isPdfFile(file) || isZipFile(file);
  }

  /**
   * 허용된 문서 파일 타입 확인 (PDF, PNG, JPEG)
   *
   * @param file 검증할 파일
   * @return 허용된 문서 파일인 경우 true
   */
  public boolean isAllowedDocumentFile(MultipartFile file) {
    return isPdfFile(file) || isValidImageFile(file);
  }

  /**
   * 특정 파일 확장자들 중 하나에 해당하는지 확인
   *
   * @param file 검증할 파일
   * @param allowedExtensions 허용된 확장자 배열 (예: "pdf", "jpg", "png")
   * @return 허용된 확장자 중 하나에 해당하는 경우 true
   */
  public boolean hasAllowedExtension(MultipartFile file, String... allowedExtensions) {
    if (!hasValidFileName(file) || allowedExtensions == null || allowedExtensions.length == 0) {
      return false;
    }

    String fileName = file.getOriginalFilename().toLowerCase();
    return Arrays.stream(allowedExtensions)
        .anyMatch(ext -> fileName.endsWith("." + ext.toLowerCase()));
  }

  /**
   * 파일 크기가 제한을 초과하는지 확인
   *
   * @param file 검증할 파일
   * @param maxSizeInBytes 최대 크기 (바이트)
   * @return 제한을 초과하는 경우 true
   */
  public boolean exceedsMaxSize(MultipartFile file, long maxSizeInBytes) {
    return file != null && file.getSize() > maxSizeInBytes;
  }

  /**
   * 파일 크기를 MB 단위로 확인
   *
   * @param file 검증할 파일
   * @param maxSizeInMB 최대 크기 (MB)
   * @return 제한을 초과하는 경우 true
   */
  public boolean exceedsMaxSizeMB(MultipartFile file, int maxSizeInMB) {
    return exceedsMaxSize(file, maxSizeInMB * 1024L * 1024L);
  }

  /**
   * 종합 파일 검증
   *
   * @param file 검증할 파일
   * @param fileType 파일 타입 (IMAGE, PDF, ZIP, DOCUMENT)
   * @param maxSizeInMB 최대 크기 (MB)
   * @return 검증 결과
   */
  public FileValidationResult validateFile(MultipartFile file, FileType fileType, int maxSizeInMB) {
    if (isEmpty(file)) {
      return FileValidationResult.fail("파일을 선택해주세요.");
    }

    if (!hasValidFileName(file)) {
      return FileValidationResult.fail("유효한 파일명이 필요합니다.");
    }

    if (exceedsMaxSizeMB(file, maxSizeInMB)) {
      return FileValidationResult.fail(String.format("파일 크기는 %dMB 이하여야 합니다.", maxSizeInMB));
    }

    boolean isValid =
        switch (fileType) {
          case IMAGE -> isImageFile(file);
          case STRICT_IMAGE -> isValidImageFile(file);
          case PDF -> isPdfFile(file);
          case ZIP -> isZipFile(file);
          case PDF_OR_ZIP -> isPdfOrZipFile(file);
          case DOCUMENT -> isAllowedDocumentFile(file);
        };

    if (!isValid) {
      String allowedTypes =
          switch (fileType) {
            case IMAGE, STRICT_IMAGE -> "이미지 파일만";
            case PDF -> "PDF 파일만";
            case ZIP -> "ZIP 파일만";
            case PDF_OR_ZIP -> "PDF 또는 ZIP 파일만";
            case DOCUMENT -> "PDF, PNG, JPEG 파일만";
          };
      return FileValidationResult.fail(allowedTypes + " 업로드 가능합니다.");
    }

    return FileValidationResult.success();
  }

  /** 지원하는 파일 타입 enum */
  public enum FileType {
    IMAGE, // 모든 이미지 (image/*로 시작)
    STRICT_IMAGE, // 엄격한 이미지 (JPEG, PNG만)
    PDF, // PDF 파일만
    ZIP, // ZIP 파일만
    PDF_OR_ZIP, // PDF 또는 ZIP
    DOCUMENT // 문서 파일 (PDF, PNG, JPEG)
  }

  /** 파일 검증 결과 클래스 */
  public static class FileValidationResult {
    private final boolean valid;
    private final String errorMessage;

    private FileValidationResult(boolean valid, String errorMessage) {
      this.valid = valid;
      this.errorMessage = errorMessage;
    }

    public static FileValidationResult success() {
      return new FileValidationResult(true, null);
    }

    public static FileValidationResult fail(String errorMessage) {
      return new FileValidationResult(false, errorMessage);
    }

    public boolean isValid() {
      return valid;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }
}
