package liaison.groble.domain.product.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.product.enums.CoachingPeriod;
import liaison.groble.domain.product.enums.CoachingType;
import liaison.groble.domain.product.enums.DocumentProvision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coaching_options")
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class CoachingOption {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  // 옵션 이름
  @Column(name = "option_name")
  private String name;

  // 옵션 설명
  @Column(name = "option_description")
  private String description;

  // 코칭 기간
  @Column(name = "coaching_period")
  @Enumerated(value = STRING)
  private CoachingPeriod coachingPeriod;

  // 자료 제공 (제공/미제공)
  @Column(name = "document_provision")
  @Enumerated(value = STRING)
  private DocumentProvision documentProvision;

  // 코칭 방식 (온라인/오프라인)
  @Column(name = "coaching_type")
  @Enumerated(value = STRING)
  private CoachingType coachingType;

  @Column(name = "coaching_type_description")
  private String coachingTypeDescription;

  private BigDecimal price;
}
