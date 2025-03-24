package liaison.groblecore.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor
public class Role {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, unique = true, nullable = false)
  private RoleType name;

  public Role(RoleType name) {
    this.name = name;
  }

  public static Role of(RoleType roleType) {
    return new Role(roleType);
  }
}
