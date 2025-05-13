package liaison.groble.security.adapter;

import org.springframework.security.core.GrantedAuthority;

import liaison.groble.domain.role.Role;

public class RoleAdapter implements GrantedAuthority {
  private final Role role;

  public RoleAdapter(Role role) {
    this.role = role;
  }

  @Override
  public String getAuthority() {
    return role.getName();
  }
}
