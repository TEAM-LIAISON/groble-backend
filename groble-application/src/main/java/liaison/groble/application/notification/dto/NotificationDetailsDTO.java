package liaison.groble.application.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDetailsDTO {

  // Purchase
  private Long contentId;
  private Long reviewId;
  private String merchantUid;

  // Sell
  private Long purchaseId;

  // 공통 필드
  private String nickname;

  // Review
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
  public static NotificationDetailsDTO contentReviewed(
      Long contentId, Long reviewId, String thumbnailUrl) {
    return NotificationDetailsDTO.builder()
        .contentId(contentId)
        .reviewId(reviewId)
        .thumbnailUrl(thumbnailUrl)
        .build();
  }

  /** Factory method for WELCOME_GROBLE notifications */
  public static NotificationDetailsDTO welcomeGroble(String nickname, String systemTitle) {
    return NotificationDetailsDTO.builder().nickname(nickname).systemTitle(systemTitle).build();
  }

  /** Factory method for CONTENT_REVIEW_REPLY notifications */
  public static NotificationDetailsDTO contentReviewReplied(Long contentId, Long reviewId) {
    return NotificationDetailsDTO.builder().contentId(contentId).reviewId(reviewId).build();
  }

  /** Factory method for CONTENT_PURCHASED notifications */
  public static NotificationDetailsDTO contentPurchased(Long contentId, String merchantUid) {
    return NotificationDetailsDTO.builder().contentId(contentId).merchantUid(merchantUid).build();
  }

  /** Factory method for CONTENT_SOLD notifications */
  public static NotificationDetailsDTO contentSold(Long contentId, Long purchaseId) {
    return NotificationDetailsDTO.builder().contentId(contentId).purchaseId(purchaseId).build();
  }

  /** Factory method for CONTENT_SOLD_STOPPED notifications */
  public static NotificationDetailsDTO contentSoldStopped(Long contentId) {
    return NotificationDetailsDTO.builder().contentId(contentId).build();
  }
}
