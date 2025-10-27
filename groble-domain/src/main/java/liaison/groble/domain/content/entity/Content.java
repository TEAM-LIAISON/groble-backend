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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.enums.AdminContentCheckingStatus;
import liaison.groble.domain.content.enums.ContentPaymentType;
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

  @Column(name = "payment_type", nullable = false)
  @Enumerated(value = STRING)
  private ContentPaymentType paymentType = ContentPaymentType.ONE_TIME;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ContentOption> options = new ArrayList<>();

  // 썸네일 URL
  @Lob
  @Column(columnDefinition = "TEXT")
  private String thumbnailUrl;

  // 콘텐츠 소개
  // Editor 내부에 들어가는 값으로 Markdown 형식으로 저장됩니다
  @Lob
  @Column(columnDefinition = "TEXT")
  private String contentIntroduction;

  // 서비스 타겟
  @Column(name = "service_target", length = 1000)
  private String serviceTarget;

  // 제공 절차
  @Column(name = "service_process", length = 1000)
  private String serviceProcess;

  // 메이커 소개
  @Column(name = "maker_intro", length = 1000)
  private String makerIntro;

  // 판매 수
  @Column(name = "sale_count")
  private Integer saleCount = 0;

  // 콘텐츠 상태
  // DRAFT, ACTIVE, DELETED, DISCONTINUED
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContentStatus status = ContentStatus.DRAFT;

  // 관리자 콘텐츠 확인 상태
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AdminContentCheckingStatus adminContentCheckingStatus =
      AdminContentCheckingStatus.PENDING;

  // 최저가
  @Column(name = "lowest_price")
  private BigDecimal lowestPrice;

  // 정렬 순서
  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;

  // 콘텐츠 반려 사유
  @Lob
  @Column(name = "reject_reason", columnDefinition = "TEXT")
  private String rejectReason;

  // 콘텐츠 조회수
  @Column(name = "view_count", nullable = false)
  private Long viewCount = 0L;

  @Column(name = "is_representative", nullable = false)
  private Boolean isRepresentative = false;

  // 검색 엔진 노출 여부
  @Column(name = "is_search_exposed", nullable = false)
  private Boolean isSearchExposed = true;

  // 비즈니스 로직으로 옵션 유형 검증
  public void addOption(ContentOption option) {
    if (contentType == ContentType.DOCUMENT && !(option instanceof DocumentOption)) {
      throw new IllegalArgumentException("문서 콘텐츠에는 문서 옵션만 추가할 수 있습니다.");
    }
    options.add(option);
    option.setContent(this);
  }

  // Setter 메서드 추가
  public void setTitle(String title) {
    this.title = title;
  }

  public void setContentType(ContentType contentType) {
    this.contentType = contentType;
  }

  public void setPaymentType(ContentPaymentType paymentType) {
    this.paymentType = paymentType != null ? paymentType : ContentPaymentType.ONE_TIME;
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

  public void setRepresentative(Boolean isRepresentative) {
    this.isRepresentative = isRepresentative;
  }

  public void setSearchExposed(Boolean isSearchExposed) {
    this.isSearchExposed = isSearchExposed;
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
}
