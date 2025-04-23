package liaison.groble.domain.product.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.apache.commons.codec.digest.DigestUtils;

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProduct extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "access_count")
  private int accessCount = 0;

  @Column(name = "last_accessed_at")
  private LocalDateTime lastAccessedAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Column(name = "download_url_hash", unique = true)
  private String downloadUrlHash;

  // 선택된 옵션 정보 추가
  @Column(name = "selected_option_type")
  @Enumerated(EnumType.STRING)
  private Payment.SelectedOptionType selectedOptionType;

  @Column(name = "selected_option_id")
  private Long selectedOptionId;

  @Column(name = "option_name")
  private String optionName;

  @Builder
  public UserProduct(
      User user,
      Product product,
      Order order,
      LocalDateTime expiresAt,
      Payment.SelectedOptionType selectedOptionType,
      Long selectedOptionId,
      String optionName) {
    this.user = user;
    this.product = product;
    this.order = order;
    this.expiresAt = expiresAt;
    this.selectedOptionType = selectedOptionType;
    this.selectedOptionId = selectedOptionId;
    this.optionName = optionName;
    this.downloadUrlHash = generateDownloadUrlHash();
  }

  // 비즈니스 메서드
  public void increaseAccessCount() {
    this.accessCount++;
    this.lastAccessedAt = LocalDateTime.now();
  }

  public boolean isExpired() {
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
  }

  public void renewExpiry(long days) {
    LocalDateTime now = LocalDateTime.now();
    if (this.expiresAt == null || this.expiresAt.isBefore(now)) {
      this.expiresAt = now.plusDays(days);
    } else {
      this.expiresAt = this.expiresAt.plusDays(days);
    }
  }

  // 고정된 URL 해시 생성 (문제 해결)
  private String generateDownloadUrlHash() {
    // 시크릿 키 (실제 구현에서는 설정에서 가져옴)
    String secretKey = "APP_SECRET_KEY";

    // 사용자 ID + 상품 ID + 옵션 ID + 시크릿 키 조합
    String baseInput =
        user.getId() + "-" + product.getId() + "-" + selectedOptionId + "-" + secretKey;

    // SHA-256 해시 생성 및 앞 16자리만 사용
    String fullHash = DigestUtils.sha256Hex(baseInput);
    return fullHash.substring(0, 16);
  }
}
