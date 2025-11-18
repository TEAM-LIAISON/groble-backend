package liaison.groble.api.model.admin.dashboard.response;

import java.time.LocalDateTime;
import java.util.List;

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
@Schema(description = "실시간 활성 회원 세션 정보")
public class AdminActiveMemberSessionResponse {

  @Schema(description = "세션 키", example = "member:42:9a8b7c...")
  private String sessionKey;

  @Schema(description = "회원 ID", example = "42")
  private Long userId;

  @Schema(description = "닉네임", example = "growth_maker")
  private String nickname;

  @Schema(description = "이메일", example = "maker@example.com")
  private String email;

  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phoneNumber;

  @Schema(description = "계정 타입", example = "INTEGRATED")
  private String accountType;

  @Schema(description = "마지막 사용자 유형", example = "SELLER")
  private String lastUserType;

  @Schema(description = "보유 권한")
  private List<String> roles;

  @Schema(description = "요청 URI", example = "/api/v1/market/contents/abc")
  private String requestUri;

  @Schema(description = "HTTP 메서드", example = "GET")
  private String httpMethod;

  @Schema(description = "쿼리스트링", example = "page=1&size=10")
  private String queryString;

  @Schema(description = "리퍼러", example = "https://groble.im/")
  private String referer;

  @Schema(description = "클라이언트 IP", example = "203.0.113.10")
  private String clientIp;

  @Schema(description = "User-Agent", example = "Mozilla/5.0 ...")
  private String userAgent;

  @Schema(description = "마지막 확인 시각", example = "2025-01-08 14:23:12")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime lastSeenAt;
}
