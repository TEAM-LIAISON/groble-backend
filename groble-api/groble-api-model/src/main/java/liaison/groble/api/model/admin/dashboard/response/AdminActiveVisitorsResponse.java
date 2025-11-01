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
@Schema(description = "관리자 대시보드 실시간 방문자 목록 응답")
public class AdminActiveVisitorsResponse {

  @Schema(description = "관측 윈도우(분)", example = "5")
  private int windowMinutes;

  @Schema(description = "요청된 최대 세션 수", example = "50")
  private int limit;

  @Schema(description = "데이터 기준 시각", example = "2025-01-08 14:23:12")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime generatedAt;

  @Schema(description = "활성 회원 세션 목록")
  private List<AdminActiveMemberSessionResponse> memberSessions;

  @Schema(description = "활성 게스트/비로그인 세션 목록")
  private List<AdminActiveGuestSessionResponse> guestSessions;

  @Schema(description = "활성 회원 세션 수", example = "12")
  private int memberCount;

  @Schema(description = "활성 게스트/비로그인 세션 수", example = "5")
  private int guestCount;
}
