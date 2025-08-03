package liaison.groble.external.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.metrics.AwsSdkMetrics;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
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
  }

  @Bean
  public AmazonS3 amazonS3Client() {
    log.info("=== S3 Client 생성 시작 ===");

    BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
    ClientConfiguration clientConfig = new ClientConfiguration();
    // RequestMetricCollector.NONE 을 넘겨 메트릭 수집을 완전히 비활성화
    AmazonS3Client s3 = new AmazonS3Client(
            new AWSStaticCredentialsProvider(creds),
            clientConfig,
            RequestMetricCollector.NONE
    );

    s3.setRegion(Region.getRegion(Regions.fromName(region)));

    log.info("=== S3 Client 생성 완료 ===");
    return s3;
  }
}