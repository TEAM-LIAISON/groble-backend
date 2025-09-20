package liaison.groble.domain.content.entity;

import static lombok.AccessLevel.PROTECTED;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.enums.ReviewStatus;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "content_reviews",
    indexes = {
      @Index(
          name = "idx_content_reviews_content_active_recent",
          columnList = "content_id, review_status, created_at DESC"),
      @Index(name = "idx_content_reviews_user_active", columnList = "user_id, review_status"),
      @Index(
          name = "idx_content_reviews_guest_user_active",
          columnList = "guest_user_id, review_status"),
      @Index(
          name = "idx_content_reviews_content_rating",
          columnList = "content_id, rating, review_status"),
      @Index(name = "idx_content_reviews_purchase", columnList = "purchase_id")
    })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class ContentReview extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guest_user_id")
  private GuestUser guestUser;

  @OneToOne
  @JoinColumn(name = "purchase_id", nullable = false)
  private Purchase purchase;

  @Column(precision = 2, scale = 1) // DECIMAL(2,1) 별점
  private BigDecimal rating; // 0.0 ~ 5.0

  @Column(nullable = false, length = 1000)
  private String reviewContent;

  @Column(name = "review_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private ReviewStatus reviewStatus = ReviewStatus.ACTIVE;

  @Column(name = "deletion_requested_at")
  private LocalDateTime deletionRequestedAt;

  public void updateReview(BigDecimal rating, String reviewContent) {
    this.rating = rating;
    this.reviewContent = reviewContent;
  }

  public void reactivate(BigDecimal rating, String reviewContent) {
    updateReview(rating, reviewContent);
    this.reviewStatus = ReviewStatus.ACTIVE;
    this.deletionRequestedAt = null;
  }

  // 유틸리티 메서드들
  /**
   * 회원 리뷰인지 확인
   *
   * @return 회원 리뷰이면 true, 비회원 리뷰이면 false
   */
  public boolean isMemberReview() {
    return this.user != null;
  }

  /**
   * 비회원 리뷰인지 확인
   *
   * @return 비회원 리뷰이면 true, 회원 리뷰이면 false
   */
  public boolean isGuestReview() {
    return this.guestUser != null;
  }

  /**
   * 리뷰 작성자 ID 반환 (회원이면 userId, 비회원이면 guestUserId)
   *
   * @return 리뷰 작성자 ID
   */
  public Long getReviewerId() {
    if (isMemberReview()) {
      return this.user.getId();
    } else if (isGuestReview()) {
      return this.guestUser.getId();
    }
    return null;
  }

  /**
   * 리뷰 작성자 이름 반환 (회원이면 닉네임, 비회원이면 사용자명)
   *
   * @return 리뷰 작성자 이름
   */
  public String getReviewerName() {
    if (isMemberReview()) {
      return this.user.getNickname();
    } else if (isGuestReview()) {
      return this.guestUser.getUsername();
    }
    return null;
  }
}
