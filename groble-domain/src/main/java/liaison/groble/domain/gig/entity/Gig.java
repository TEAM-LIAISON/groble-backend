package liaison.groble.domain.gig.entity;

import static jakarta.persistence.EnumType.STRING;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import liaison.groble.domain.gig.enums.GigStatus;
import liaison.groble.domain.product.enums.GigType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gigs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class Gig {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "gig", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GigOption> options = new ArrayList<>();

  @Column(nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private GigStatus status = GigStatus.DRAFT;

  @Column(name = "content_type")
  @Enumerated(value = STRING)
  private GigType gigType;

  private String thumbnailUrl; // 썸네일 이미지 URL

  @Column(name = "sale_count")
  private Integer saleCount = 0; // 판매 수

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
}
