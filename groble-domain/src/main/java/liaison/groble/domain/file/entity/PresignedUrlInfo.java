package liaison.groble.domain.file.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PresignedUrlInfo {
  private String presignedUrl;
  private String fileUrl;
  private String fileKey;
  private long expirationTime;
}
