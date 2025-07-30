package liaison.groble.domain.content.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;

import lombok.Getter;

@Getter
@Entity
@Table(name = "content_options")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "option_type")
public abstract class ContentOption extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id")
  private Content content;

  // 옵션 제목
  @Column(length = 20)
  private String name;

  // 옵션 설명
  @Column(length = 60)
  private String description;

  // 옵션 가격
  private BigDecimal price;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "deactivated_at")
  private LocalDateTime deactivatedAt;

  // 공통 필드 업데이트 메소드
  public void updateCommonFields(String name, String description, BigDecimal price) {
    this.name = name;
    this.description = description;
    this.price = price;
  }

  // 활성화 상태 확인 메소드
  public boolean isActive() {
    return Boolean.TRUE.equals(isActive);
  }

  // 비활성화 메소드
  public void deactivate() {
    this.isActive = false;
    this.deactivatedAt = LocalDateTime.now();
  }

  // 콘텐츠 설정
  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public void setContent(Content content) {
    this.content = content;
  }
}
