package liaison.groble.domain.content.entity;

import static jakarta.persistence.EnumType.STRING;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import liaison.groble.domain.common.entity.BaseEntity;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "contents",
    indexes = {
      @Index(name = "idx_content_user_id", columnList = "user_id"),
      @Index(name = "idx_content_category_id", columnList = "category_id"),
      @Index(name = "idx_content_status", columnList = "status"),
      @Index(name = "idx_content_user_status", columnList = "user_id, status"),
      @Index(name = "idx_content_user_category", columnList = "user_id, category_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class Content extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 컨텐츠 이름
  @Column(nullable = false)
  private String title;

  // 컨텐츠 유형
  @Column(name = "content_type")
  @Enumerated(value = STRING)
  private ContentType contentType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 카테고리
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  // 컨텐츠 유형에 따른 옵션
  @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ContentOption> options = new ArrayList<>();

  private String thumbnailUrl; // 썸네일 URL
  private String serviceTarget; // 서비스 타겟
  private String serviceProcess; // 제공 절차
  private String makerIntro; // 메이커 소개

  @Column(name = "sale_count")
  private Integer saleCount = 0; // 판매 수

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContentStatus status = ContentStatus.DRAFT;

  @Column(name = "lowest_price")
  private BigDecimal lowestPrice; // 최저가

  // 비즈니스 로직으로 옵션 유형 검증
  public void addOption(ContentOption option) {
    if (contentType == ContentType.COACHING && !(option instanceof CoachingOption)) {
      throw new IllegalArgumentException("코칭 상품에는 코칭 옵션만 추가할 수 있습니다.");
    }
    if (contentType == ContentType.DOCUMENT && !(option instanceof DocumentOption)) {
      throw new IllegalArgumentException("문서 상품에는 문서 옵션만 추가할 수 있습니다.");
    }
    options.add(option);
    option.setProduct(this);
  }

  // Setter 메서드 추가
  public void setUser(User user) {
    this.user = user;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setContentType(ContentType contentType) {
    this.contentType = contentType;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public void setStatus(ContentStatus status) {
    this.status = status;
  }

  public void incrementSaleCount() {
    this.saleCount = this.saleCount + 1;
  }

  public Content(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null when creating a Content");
    }
    this.user = user;
    this.saleCount = 0;
    this.options = new ArrayList<>();
  }

  // 팩토리 메서드
  public static Content createDraft(User user) {
    return new Content(user);
  }
}
