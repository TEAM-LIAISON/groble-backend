package liaison.groble.domain.content.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "content_replies",
    indexes = {
      @Index(
          name = "idx_content_replies_review_active_recent",
          columnList = "content_review_id, is_deleted, created_at DESC"),
      @Index(name = "idx_content_replies_seller_active", columnList = "seller_id, is_deleted")
    })
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class ContentReply extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_review_id", nullable = false)
  private ContentReview contentReview;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @Column(nullable = false, length = 500)
  private String replyContent;

  @Column(nullable = false)
  private boolean isDeleted;
}
