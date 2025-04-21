package liaison.groble.domain.terms;

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

import liaison.groble.domain.terms.enums.TermsType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "terms",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_terms_type_version",
          columnNames = {"type", "version"})
    })
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Terms {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "terms_title", nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TermsType type;

  @Column(nullable = false)
  private String version; // ex: 20240410.173000

  private String contentUrl; // 외부 Notion, PDF, CMS 링크 등

  @Column(nullable = false)
  private LocalDateTime effectiveFrom;

  @Column private LocalDateTime effectiveTo;

  @OneToMany(mappedBy = "terms")
  private List<UserTerms> agreements = new ArrayList<>();

  @Builder
  public Terms(
      String title,
      TermsType type,
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
