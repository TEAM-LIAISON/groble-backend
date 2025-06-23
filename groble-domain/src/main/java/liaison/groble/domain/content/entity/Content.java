package liaison.groble.domain.content.entity;

import static jakarta.persistence.EnumType.STRING;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.enums.AdminContentCheckingStatus;
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
      @Index(name = "idx_content_user_category", columnList = "user_id, category_id"),
      @Index(
          name = "ux_user_representative_content",
          columnList = "user_id, is_representative",
          unique = true)
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class Content extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 30)
  private String title;

  // 콘텐츠 유형
  @Column(name = "content_type")
  @Enumerated(value = STRING)
  private ContentType contentType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ContentOption> options = new ArrayList<>();

  @Lob
  @Column(columnDefinition = "TEXT")
  private String thumbnailUrl; // 썸네일 URL

  @Lob
  @Column(columnDefinition = "TEXT")
  private String contentIntroduction; // 콘텐츠 소개

  @ElementCollection
  @CollectionTable(
      name = "content_detail_image_urls",
      joinColumns = @JoinColumn(name = "content_id"))
  @Column(name = "image_url")
  private List<String> contentDetailImageUrls = new ArrayList<>();

  @Column(name = "service_target", length = 1000)
  private String serviceTarget; // 서비스 타겟

  @Column(name = "service_process", length = 1000)
  private String serviceProcess; // 제공 절차

  @Column(name = "maker_intro", length = 1000)
  private String makerIntro; // 메이커 소개

  @Column(name = "sale_count")
  private Integer saleCount = 0; // 판매 수

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContentStatus status = ContentStatus.DRAFT;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AdminContentCheckingStatus adminContentCheckingStatus =
      AdminContentCheckingStatus.PENDING;

  @Column(name = "lowest_price")
  private BigDecimal lowestPrice; // 최저가

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;

  // 값이 클수록(예: 100) 우선순위가 높다고 가정. 기본은 0.

  @Lob
  @Column(name = "reject_reason", columnDefinition = "TEXT")
  private String rejectReason;

  /** 조회수 */
  @Column(name = "view_count", nullable = false)
  private Long viewCount = 0L;

  @Column(name = "is_representative", nullable = false)
  private boolean isRepresentative = false;

  // 비즈니스 로직으로 옵션 유형 검증
  public void addOption(ContentOption option) {
    if (contentType == ContentType.COACHING && !(option instanceof CoachingOption)) {
      throw new IllegalArgumentException("코칭 콘텐츠에는 코칭 옵션만 추가할 수 있습니다.");
    }
    if (contentType == ContentType.DOCUMENT && !(option instanceof DocumentOption)) {
      throw new IllegalArgumentException("문서 콘텐츠에는 문서 옵션만 추가할 수 있습니다.");
    }
    options.add(option);
    option.setContent(this);
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

  public void setServiceTarget(String serviceTarget) {
    this.serviceTarget = serviceTarget;
  }

  public void setServiceProcess(String serviceProcess) {
    this.serviceProcess = serviceProcess;
  }

  public void setMakerIntro(String makerIntro) {
    this.makerIntro = makerIntro;
  }

  public void setStatus(ContentStatus status) {
    this.status = status;
  }

  public void setAdminContentCheckingStatus(AdminContentCheckingStatus adminContentCheckingStatus) {
    this.adminContentCheckingStatus = adminContentCheckingStatus;
  }

  public void setLowestPrice(BigDecimal lowestPrice) {
    this.lowestPrice = lowestPrice;
  }

  public void setRejectReason(String rejectReason) {
    this.rejectReason = rejectReason;
  }

  public void setContentIntroduction(String contentIntroduction) {
    this.contentIntroduction = contentIntroduction;
  }

  // Content 클래스에 추가
  public void setContentDetailImageUrls(List<String> urls) {
    this.contentDetailImageUrls.clear();
    if (urls != null) {
      this.contentDetailImageUrls.addAll(urls);
    }
  }

  public void addContentDetailImageUrl(String url) {
    if (url != null && !url.isBlank()) {
      this.contentDetailImageUrls.add(url);
    }
  }

  public void removeContentDetailImageUrl(String url) {
    this.contentDetailImageUrls.remove(url);
  }

  public void incrementSaleCount() {
    this.saleCount = this.saleCount + 1;
  }

  /** 조회수 1 증가 */
  public void incrementViewCount() {
    this.viewCount = this.viewCount + 1;
  }

  public Content(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null when creating a Content");
    }
    this.user = user;
    this.saleCount = 0;
    this.options = new ArrayList<>();
    this.sortOrder = 0; // 기본값
  }

  // sortOrder setter 추가
  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  // 팩토리 메서드
  public static Content createDraft(User user) {
    return new Content(user);
  }
}
