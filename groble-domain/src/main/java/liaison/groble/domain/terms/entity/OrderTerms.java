package liaison.groble.domain.terms.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import liaison.groble.domain.terms.enums.OrderTermsType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "order_terms",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_order_terms_type_version",
          columnNames = {"type", "version"})
    })
@Getter
@NoArgsConstructor(access = PROTECTED)
public class OrderTerms {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_terms_title", nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderTermsType type;

  @Column(nullable = false)
  private String version;

  private String contentUrl;

  @Column(nullable = false)
  private LocalDateTime effectiveFrom;

  @Column private LocalDateTime effectiveTo;

  @OneToMany(mappedBy = "orderTerms")
  private List<UserOrderTerms> agreements = new ArrayList<>();

  @Builder
  public OrderTerms(
      String title,
      OrderTermsType type,
      String version,
      String contentUrl,
      LocalDateTime effectiveFrom) {
    this.title = title;
    this.type = type;
    this.version = version;
    this.contentUrl = contentUrl;
    this.effectiveFrom = effectiveFrom;
  }

  public void updateEffectiveTo(LocalDateTime effectiveTo) {
    this.effectiveTo = effectiveTo;
  }
}
