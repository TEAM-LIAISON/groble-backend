package liaison.groble.domain.file.service;

import java.io.InputStream;

public interface FileStorageService {
  /**
   * 파일을 S3에 업로드합니다.
   *
   * @param inputStream 파일 데이터
   * @param fileName 저장할 파일명
   * @param contentType 파일 타입
   * @param directory 저장할 디렉토리
   * @return 업로드된 파일 URL
   */
  String uploadFile(InputStream inputStream, String fileName, String contentType, String directory);

  /**
   * 파일을 삭제합니다.
   *
   * @param fileKey S3 파일 키
   */
  void deleteFile(String fileKey);
}
