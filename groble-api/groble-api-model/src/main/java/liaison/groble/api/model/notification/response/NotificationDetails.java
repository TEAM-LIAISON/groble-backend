package liaison.groble.api.model.notification.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@Schema(description = "알림 상세 응답")
public class NotificationDetails {

  @Schema(description = "닉네임", example = "user123")
  private String nickname;

  // 판매자 알림 관련 필드
  @Schema(description = "판매자 인증 여부 (SELLER 타입에서 사용)", example = "true")
  private Boolean isVerified;

  // 콘텐츠 알림 관련 필드
  @Schema(description = "콘텐츠 ID (CONTENT 타입에서 사용)", example = "12345")
  private Long contentId;

  @Schema(description = "썸네일 URL (CONTENT 타입에서 사용)", example = "https://example.com/thumbnail.jpg")
  private String thumbnailUrl;

  @Schema(description = "콘텐츠 승인 여부 (CONTENT 타입에서 사용)", example = "true")
  private Boolean isContentApproved;

  // 시스템 알림 관련 필드
  @Schema(description = "시스템 알림 제목 (SYSTEM 타입에서 사용)", example = "Groble에 오신 것을 환영합니다!")
  private String systemTitle;

  @Builder
  private NotificationDetails(
      final String nickname,
      final Boolean isVerified,
      final Long contentId,
      final String thumbnailUrl,
      final Boolean isContentApproved,
      final String systemTitle) {
    this.nickname = nickname;
    this.isVerified = isVerified;
    this.contentId = contentId;
    this.thumbnailUrl = thumbnailUrl;
    this.isContentApproved = isContentApproved;
    this.systemTitle = systemTitle;
  }

  // 그로블 환영 알림
  public static NotificationDetails welcomeGroble(final String nickname, final String systemTitle) {
    return NotificationDetails.builder().nickname(nickname).systemTitle(systemTitle).build();
  }
}
