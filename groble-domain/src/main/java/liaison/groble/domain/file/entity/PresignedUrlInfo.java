package liaison.groble.domain.file.entity;

import java.time.Instant;

import lombok.Builder;

@Builder
public class PresignedUrlInfo {
  private String key;
  private String url;
  private Instant expiration;
  private String contentType;
}
