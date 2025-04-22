package liaison.groble.external.s3;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import liaison.groble.domain.file.entity.PresignedUrlInfo;
import liaison.groble.domain.file.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  @PostConstruct
  public void init() {
    log.info("▶ S3FileStorageService initialized for bucket={}", bucketName);
  }

  @Override
  public String uploadFile(
      InputStream inputStream, String fileName, String contentType, String directory) {
    String key = directory.endsWith("/") ? directory + fileName : directory + "/" + fileName;

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    // metadata.setContentLength(...) 필요 시 파일 크기 설정

    try {
      amazonS3.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
      URL url = amazonS3.getUrl(bucketName, key);
      String resultUrl = url.toString();
      log.debug("✅ Uploaded S3 object: {}/{} → {}", bucketName, key, resultUrl);
      return resultUrl;
    } catch (Exception e) {
      log.error("❌ S3 upload failed for key={}", key, e);
      throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
    }
  }

  @Override
  public PresignedUrlInfo generatePresignedUrl(
      String fileName, String contentType, String directory) {
    String key = directory.endsWith("/") ? directory + fileName : directory + "/" + fileName;

    // presign URL 유효 시간
    Instant expirationTime = Instant.now().plus(Duration.ofMinutes(15));
    Date expiration = Date.from(expirationTime);

    GeneratePresignedUrlRequest req =
        new GeneratePresignedUrlRequest(bucketName, key)
            .withMethod(HttpMethod.PUT)
            .withExpiration(expiration)
            .withContentType(contentType);

    URL url = amazonS3.generatePresignedUrl(req);
    log.debug("🔑 Generated presigned URL: {}", url);

    return PresignedUrlInfo.builder()
        .key(key)
        .url(url.toString())
        .expiration(expirationTime)
        .contentType(contentType)
        .build();
  }

  @Override
  public void deleteFile(String fileKey) {
    try {
      amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileKey));
      log.debug("🗑 Deleted S3 object: {}/{}", bucketName, fileKey);
    } catch (Exception e) {
      log.error("❌ S3 delete failed for key={}", fileKey, e);
      throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage(), e);
    }
  }
}
