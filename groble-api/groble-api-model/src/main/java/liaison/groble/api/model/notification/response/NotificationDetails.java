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

  // Common fields
  @Schema(description = "닉네임")
  private String nickname;

  // Seller fields
  @Schema(description = "판매자 인증 여부")
  private Boolean isVerified;

  // Content fields
  @Schema(description = "콘텐츠 ID")
  private Long contentId;

  @Schema(description = "썸네일 URL")
  private String thumbnailUrl;

  @Schema(description = "콘텐츠 승인 여부")
  private Boolean isContentApproved;

  // System fields
  @Schema(description = "시스템 알림 제목")
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
