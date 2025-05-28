package liaison.groble.application.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDetailsDto {

  // 공통 필드
  private String nickname;

  // Review
  private Long contentId;
  private String thumbnailUrl;

  // System
  private String systemTitle;

  /** Factory method for MAKER_CERTIFIED notifications */
  public static NotificationDetailsDto makerCertified(String nickname) {
    return NotificationDetailsDto.builder().nickname(nickname).build();
  }

  /** Factory method for MAKER_CERTIFY_REJECTED notifications */
  public static NotificationDetailsDto makerCertifyRejected(String nickname) {
    return NotificationDetailsDto.builder().nickname(nickname).build();
  }

  /** Factory method for CONTENT_REVIEW_APPROVED notifications */
  public static NotificationDetailsDto contentReviewApproved(Long contentId, String thumbnailUrl) {
    return NotificationDetailsDto.builder().contentId(contentId).thumbnailUrl(thumbnailUrl).build();
  }

  /** Factory method for CONTENT_REVIEW_REJECTED notifications */
  public static NotificationDetailsDto contentReviewRejected(Long contentId, String thumbnailUrl) {
    return NotificationDetailsDto.builder().contentId(contentId).thumbnailUrl(thumbnailUrl).build();
  }

  /** Factory method for WELCOME_GROBLE notifications */
  public static NotificationDetailsDto welcomeGroble(String nickname, String systemTitle) {
    return NotificationDetailsDto.builder().nickname(nickname).systemTitle(systemTitle).build();
  }
}
