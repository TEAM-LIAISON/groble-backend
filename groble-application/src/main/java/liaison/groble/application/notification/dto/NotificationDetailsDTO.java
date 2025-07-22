package liaison.groble.application.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDetailsDTO {

  // 공통 필드
  private String nickname;

  // Review
  private Long contentId;
  private String thumbnailUrl;

  // System
  private String systemTitle;

  /** Factory method for MAKER_CERTIFIED notifications */
  public static NotificationDetailsDTO makerCertified(String nickname) {
    return NotificationDetailsDTO.builder().nickname(nickname).build();
  }

  /** Factory method for MAKER_CERTIFY_REJECTED notifications */
  public static NotificationDetailsDTO makerCertifyRejected(String nickname) {
    return NotificationDetailsDTO.builder().nickname(nickname).build();
  }

  /** Factory method for CONTENT_REVIEWED notifications */
  public static NotificationDetailsDTO contentReviewed(Long contentId, String thumbnailUrl) {
    return NotificationDetailsDTO.builder().contentId(contentId).thumbnailUrl(thumbnailUrl).build();
  }

  /** Factory method for WELCOME_GROBLE notifications */
  public static NotificationDetailsDTO welcomeGroble(String nickname, String systemTitle) {
    return NotificationDetailsDTO.builder().nickname(nickname).systemTitle(systemTitle).build();
  }
}
