package liaison.groble.external.config;

import com.amazonaws.metrics.AwsSdkMetrics;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class S3Config {
  @Value("${cloud.aws.credentials.access-key}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secret-key}")
  private String secretKey;

  @Value("${cloud.aws.region.static}")
  private String region;

  @PostConstruct
  public void validateProperties() {
    log.debug("▶ S3Config loaded with accessKeyId='{}', region='{}'", accessKey, region);
    // (secretKey는 노출 금지)
    System.setProperty("com.amazonaws.sdk.disableMetricAdminMBeanRegistration", "true");
    System.setProperty("com.amazonaws.sdk.disableMetrics", "true");
  }

//  @Bean
//  public AmazonS3 amazonS3Client() {
//    BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
//    return AmazonS3ClientBuilder.standard()
//        .withRegion(region)
//        .withCredentials(new AWSStaticCredentialsProvider(creds))
//        .build();
//  }

  @Bean
  public AmazonS3 amazonS3Client() {
    try {
      log.info("=== S3 Client 생성 시작 ===");
      log.info("Access Key: {}", accessKey != null ? accessKey.substring(0, 4) + "****" : "NULL");
      log.info("Region: {}", region);
      log.info("Secret Key exists: {}", secretKey != null && !secretKey.isEmpty());

      if (accessKey == null || accessKey.isEmpty()) {
        throw new IllegalArgumentException("AWS Access Key is null or empty");
      }
      if (secretKey == null || secretKey.isEmpty()) {
        throw new IllegalArgumentException("AWS Secret Key is null or empty");
      }
      if (region == null || region.isEmpty()) {
        throw new IllegalArgumentException("AWS Region is null or empty");
      }

      BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
      log.info("AWS Credentials 생성 완료");

      AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
              .withRegion(region)
              .withCredentials(new AWSStaticCredentialsProvider(creds))
              .build();

      log.info("=== S3 Client 생성 완료 ===");
      return s3Client;

    } catch (Exception e) {
      log.error("S3 Client 생성 실패: {}", e.getMessage(), e);
      throw e;
    }
  }
}
