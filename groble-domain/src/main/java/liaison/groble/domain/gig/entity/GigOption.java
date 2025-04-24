package liaison.groble.domain.gig.entity;

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

@Entity
@Table(name = "gig_options")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "option_type")
public abstract class GigOption extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gig_id")
  private Gig gig;

  private String name;
  private String description;
  private BigDecimal price;

  public void setProduct(Gig gig) {
    this.gig = gig;
  }
}
