package liaison.groble.domain.gig.entity;

import static jakarta.persistence.EnumType.STRING;

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
import liaison.groble.domain.gig.enums.GigStatus;
import liaison.groble.domain.gig.enums.GigType;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "gigs",
    indexes = {
      @Index(name = "idx_gig_user_id", columnList = "user_id"),
      @Index(name = "idx_gig_category_id", columnList = "category_id"),
      @Index(name = "idx_gig_status", columnList = "status"),
      @Index(name = "idx_gig_user_status", columnList = "user_id, status"),
      @Index(name = "idx_gig_user_category", columnList = "user_id, category_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class Gig extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 컨텐츠 이름
  @Column(nullable = false)
  private String title;

  // 컨텐츠 유형
  @Column(name = "gig_type")
  @Enumerated(value = STRING)
  private GigType gigType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 카테고리
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  // 컨텐츠 유형에 따른 옵션
  @OneToMany(mappedBy = "gig", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GigOption> options = new ArrayList<>();

  private String thumbnailUrl; // 썸네일 이미지 URL

  @Column(name = "sale_count")
  private Integer saleCount = 0; // 판매 수

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private GigStatus status = GigStatus.DRAFT;

  // 비즈니스 로직으로 옵션 유형 검증
  public void addOption(GigOption option) {
    if (gigType == GigType.COACHING && !(option instanceof CoachingOption)) {
      throw new IllegalArgumentException("코칭 상품에는 코칭 옵션만 추가할 수 있습니다.");
    }
    if (gigType == GigType.DOCUMENT && !(option instanceof DocumentOption)) {
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

  public void setGigType(GigType gigType) {
    this.gigType = gigType;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public void setStatus(GigStatus status) {
    this.status = status;
  }

  public void incrementSaleCount() {
    this.saleCount = this.saleCount + 1;
  }

  public Gig(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null when creating a Gig");
    }
    this.user = user;
    this.saleCount = 0;
    this.options = new ArrayList<>();
  }

  // 팩토리 메서드
  public static Gig createDraft(User user) {
    return new Gig(user);
  }
}
