package liaison.groble.common.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Accessor {
  private final Long id;
  private final String email;
  private final Set<String> roles;
  private final String userType; // BUYER 또는 SELLER

  public boolean hasRole(String role) {
    return roles.contains(role);
  }

  public Long getUserId() {
    return id;
  }
}
