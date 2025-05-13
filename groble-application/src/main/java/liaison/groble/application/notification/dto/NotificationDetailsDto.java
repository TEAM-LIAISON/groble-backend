package liaison.groble.application.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDetailsDto {

  // 공통 필드
  private String nickname;

  // Seller
  private Boolean isVerified;

  // Content
  private Long contentId;
  private String thumbnailUrl;
  private Boolean isContentApproved;

  // System
  private String systemTitle;

  /** Factory method for SELLER_VERIFIED notifications */
  public static NotificationDetailsDto sellerVerified(String nickname, Boolean isVerified) {
    return NotificationDetailsDto.builder().nickname(nickname).isVerified(isVerified).build();
  }

  /** Factory method for SELLER_REJECTED notifications */
  public static NotificationDetailsDto sellerRejected(String nickname, Boolean isVerified) {
    return NotificationDetailsDto.builder().nickname(nickname).isVerified(isVerified).build();
  }

  /** Factory method for CONTENT_APPROVED notifications */
  public static NotificationDetailsDto contentApproved(
      Long contentId, String thumbnailUrl, Boolean isContentApproved) {
    return NotificationDetailsDto.builder()
        .contentId(contentId)
        .thumbnailUrl(thumbnailUrl)
        .isContentApproved(isContentApproved)
        .build();
  }

  /** Factory method for CONTENT_REJECTED notifications */
  public static NotificationDetailsDto contentRejected(
      Long contentId, String thumbnailUrl, Boolean isContentApproved) {
    return NotificationDetailsDto.builder()
        .contentId(contentId)
        .thumbnailUrl(thumbnailUrl)
        .isContentApproved(isContentApproved)
        .build();
  }

  /** Factory method for WELCOME_GROBLE notifications */
  public static NotificationDetailsDto welcomeGroble(String nickname, String systemTitle) {
    return NotificationDetailsDto.builder().nickname(nickname).systemTitle(systemTitle).build();
  }
}
