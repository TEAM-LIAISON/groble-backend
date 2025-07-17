package liaison.groble.api.model.notification.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 상세 응답")
public class NotificationDetailsResponse {

  @Schema(description = "닉네임", example = "user123")
  private String nickname;

  // 콘텐츠 알림 관련 필드
  @Schema(description = "콘텐츠 ID (CONTENT 타입에서 사용)", example = "12345")
  private Long contentId;

  @Schema(description = "썸네일 URL (CONTENT 타입에서 사용)", example = "https://example.com/thumbnail.jpg")
  private String thumbnailUrl;

  // 시스템 알림 관련 필드
  @Schema(description = "시스템 알림 제목 (SYSTEM 타입에서 사용)", example = "Groble에 오신 것을 환영합니다!")
  private String systemTitle;
}
