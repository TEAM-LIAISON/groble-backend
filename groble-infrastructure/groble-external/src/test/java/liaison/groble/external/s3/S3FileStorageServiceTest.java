package liaison.groble.external.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

  private static final String BUCKET_NAME = "test-bucket";
  private static final String CLOUD_DOMAIN = "https://cdn.test";

  @Mock private AmazonS3 amazonS3;

  private S3FileStorageService fileStorageService;

  @BeforeEach
  void setUp() {
    fileStorageService = new S3FileStorageService(amazonS3);
    ReflectionTestUtils.setField(fileStorageService, "bucketName", BUCKET_NAME);
    ReflectionTestUtils.setField(fileStorageService, "cloudDomain", CLOUD_DOMAIN);
  }

  @Test
  void uploadFile_ShouldEncodePlusSignInResultUrl() {
    // given
    ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] {});
    ArgumentCaptor<PutObjectRequest> putObjectCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);

    // when
    org.mockito.Mockito.doReturn(new PutObjectResult())
        .when(amazonS3)
        .putObject(any(PutObjectRequest.class));

    String result =
        fileStorageService.uploadFile(
            inputStream, "UUID_original+name.pdf", "application/pdf", "contents/document");

    // then - verify encoded URL
    assertEquals(
        CLOUD_DOMAIN + "/contents/document/UUID_original%2Bname.pdf",
        result,
        "Returned URL should encode '+' to '%2B'");

    // then - verify raw key used for S3 upload remains unencoded
    verify(amazonS3).putObject(putObjectCaptor.capture());
    assertEquals(
        "contents/document/UUID_original+name.pdf",
        putObjectCaptor.getValue().getKey(),
        "S3 object key should preserve original filename");
  }
}
