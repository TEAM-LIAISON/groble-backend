package liaison.groble.api.model.admin.dashboard.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "실시간 활성 게스트/비로그인 세션 정보")
public class AdminActiveGuestSessionResponse {

  @Schema(description = "세션 키", example = "guest:314:5f8d...")
  private String sessionKey;

  @Schema(description = "게스트 ID", example = "314", nullable = true)
  private Long guestId;

  @Schema(description = "게스트 토큰 인증 여부", example = "true")
  private boolean authenticated;

  @Schema(description = "게스트 표시 이름", example = "체험중 사용자", nullable = true)
  private String displayName;

  @Schema(description = "게스트 이메일", example = "guest@example.com", nullable = true)
  private String email;

  @Schema(description = "게스트 전화번호", example = "010-0000-0000", nullable = true)
  private String phoneNumber;

  @Schema(description = "익명 식별자", example = "anon-1234", nullable = true)
  private String anonymousId;

  @Schema(description = "요청 URI", example = "/api/v1/home/contents")
  private String requestUri;

  @Schema(description = "HTTP 메서드", example = "GET")
  private String httpMethod;

  @Schema(description = "쿼리스트링", example = "page=1")
  private String queryString;

  @Schema(description = "리퍼러", example = "https://groble.im/")
  private String referer;

  @Schema(description = "클라이언트 IP", example = "198.51.100.4")
  private String clientIp;

  @Schema(description = "User-Agent", example = "Mozilla/5.0 ...")
  private String userAgent;

  @Schema(description = "마지막 확인 시각", example = "2025-01-08 14:23:05")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime lastSeenAt;
}
