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
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.enums.ReviewStatus;
import liaison.groble.domain.user.entity.User;

import lombok.AllArgsConstructor;
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
          name = "idx_content_reviews_content_rating",
          columnList = "content_id, rating, review_status"),
    })
@Getter
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
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

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
}
