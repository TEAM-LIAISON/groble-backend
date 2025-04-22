package liaison.groble.external.s3;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import liaison.groble.domain.file.entity.PresignedUrlInfo;
import liaison.groble.domain.file.service.FileStorageService;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
public class S3FileStorageService implements FileStorageService {
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucketName;
  private final String cdnDomain;

  public S3FileStorageService(
      S3Client s3Client,
      S3Presigner s3Presigner,
      @Value("${cloud.aws.s3.bucket}") String bucketName,
      @Value("${cloud.aws.s3.cloud-front-image-domain:#{null}}") String cdnDomain) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.bucketName = bucketName;
    this.cdnDomain = cdnDomain;
  }

  @Override
  public String uploadFile(
      InputStream inputStream, String fileName, String contentType, String directory) {
    try {
      String fileKey = generateFileKey(directory, fileName);

      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(fileKey)
              .contentType(contentType)
              .metadata(
                  Map.of(
                      "Content-Type", contentType,
                      "Original-Filename", fileName))
              .build();

      s3Client.putObject(
          putObjectRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));

      return getFileUrl(fileKey);
    } catch (Exception e) {
      log.error("파일 업로드 중 오류 발생", e);
      throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
    }
  }

  @Override
  public PresignedUrlInfo generatePresignedUrl(
      String fileName, String contentType, String directory) {
    try {
      String fileKey = generateFileKey(directory, fileName);

      // Presigned URL 생성 (5분 유효)
      PutObjectRequest objectRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(fileKey)
              .contentType(contentType)
              .build();

      PutObjectPresignRequest presignRequest =
          PutObjectPresignRequest.builder()
              .signatureDuration(Duration.ofMinutes(5))
              .putObjectRequest(objectRequest)
              .build();

      PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

      return PresignedUrlInfo.builder()
          .presignedUrl(presignedRequest.url().toString())
          .fileUrl(getFileUrl(fileKey))
          .fileKey(fileKey)
          .expirationTime(presignedRequest.expiration().toEpochMilli())
          .build();
    } catch (Exception e) {
      log.error("Presigned URL 생성 중 오류 발생", e);
      throw new RuntimeException("Presigned URL 생성에 실패했습니다: " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteFile(String fileKey) {
    try {
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(bucketName).key(fileKey).build();

      s3Client.deleteObject(deleteObjectRequest);
    } catch (Exception e) {
      log.error("파일 삭제 중 오류 발생", e);
      throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage(), e);
    }
  }

  private String generateFileKey(String directory, String fileName) {
    // 파일명 충돌 방지를 위한 UUID 추가
    String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
    return directory + "/" + uniqueFileName;
  }

  private String getFileUrl(String fileKey) {
    // CDN 도메인이 설정된 경우 CDN URL 반환, 아니면 S3 URL 반환
    if (cdnDomain != null && !cdnDomain.isEmpty()) {
      return "https://" + cdnDomain + "/" + fileKey;
    }
    return "https://" + bucketName + ".s3.amazonaws.com/" + fileKey;
  }
}
