package liaison.groble.domain.market.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.market.exception.InvalidMarketNameException;
import liaison.groble.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마켓 정보를 관리하는 엔티티
 *
 * @author 권동민
 * @since 2025-07-27
 */
@Entity
@Table(name = "markets")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class Market extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  // 마켓 이름
  @Column(name = "market_name", length = 30)
  private String marketName;

  // 마켓 링크 URL
  @Column(name = "market_link_url", length = 32)
  private String marketLinkUrl;

  // 마켓 이름 변경 메소드
  public void changeMarketName(String marketName) {
    validateMarketName(marketName);
    this.marketName = marketName;
  }

  // 마켓 링크 URL 변경 메소드
  public void changeMarketLinkUrl(String marketLinkUrl) {
    if (marketLinkUrl != null && !marketLinkUrl.isBlank()) {
      this.marketLinkUrl = marketLinkUrl;
    }
  }

  // 마켓 이름 유효성 검사
  private void validateMarketName(String marketName) {
    if (marketName == null || marketName.isBlank()) {
      throw new InvalidMarketNameException("마켓명은 필수입니다");
    }
    if (marketName.length() > 30) {
      throw new InvalidMarketNameException("마켓명은 30자를 초과할 수 없습니다");
    }
  }
}
