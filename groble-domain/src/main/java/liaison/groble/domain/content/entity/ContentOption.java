package liaison.groble.domain.content.entity;

import java.math.BigDecimal;

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
  private String name;

  // 옵션 설명
  private String description;

  // 옵션 가격
  private BigDecimal price;

  // Setter 메서드들
  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public void setContent(Content content) {
    this.content = content;
  }
}
