package liaison.groble.domain.file.entity;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public class PresignedUrlInfo {
  private String key;
  private String url;
  private LocalDateTime expiration;
  private String contentType;
}
