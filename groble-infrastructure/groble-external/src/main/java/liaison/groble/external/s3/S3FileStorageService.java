package liaison.groble.external.s3;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

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

  @Value("${cloud.aws.s3.cloud-front-image-domain}")
  private String cloudDomain;

  @PostConstruct
  public void init() {
    log.info("▶ S3FileStorageService initialized for bucket={}", bucketName);
  }

  @Override
  public String uploadFile(
      InputStream inputStream, String fileName, String contentType, String directory) {
    // 1) 원본 키(슬래시 포함) 생성
    String rawKey = (directory.endsWith("/") ? directory : directory + "/") + fileName;

    // 2) S3에 업로드
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    amazonS3.putObject(new PutObjectRequest(bucketName, rawKey, inputStream, metadata));

    // 3) 키를 URL-안전하게 인코딩 (슬래시만 제외)
    String encodedKey = UriUtils.encodePath(rawKey, StandardCharsets.UTF_8);

    // 4) 퍼블릭 도메인 + 인코딩된 키를 합쳐서 반환
    String resultUrl = cloudDomain + "/" + encodedKey;
    log.debug("Uploaded to S3, returning URL: {}", resultUrl);
    return resultUrl;
  }

  @Override
  public PresignedUrlInfo generatePresignedUrl(
      String fileName, String contentType, String directory) {
    String key = (directory.endsWith("/") ? directory : directory + "/") + fileName;
    Date expiration = Date.from(Instant.now().plus(Duration.ofMinutes(15)));

    GeneratePresignedUrlRequest req =
        new GeneratePresignedUrlRequest(bucketName, key)
            .withMethod(HttpMethod.PUT)
            .withExpiration(expiration)
            .withContentType(contentType);

    URL presigned = amazonS3.generatePresignedUrl(req);
    // presigned URL도 커스텀 도메인 호스트로 치환
    String presignUrl = presigned.toString().replaceFirst("https://[^/]+", cloudDomain);

    return PresignedUrlInfo.builder()
        .key(key)
        .url(presignUrl)
        .expiration(expiration.toInstant())
        .contentType(contentType)
        .build();
  }

  @Override
  public void deleteFile(String fileKey) {
    amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileKey));
  }
}
