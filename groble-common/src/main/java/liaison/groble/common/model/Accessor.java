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
  private final String userType; // BUYER, SELLER, ANONYMOUS
  private final String accountType; // INTEGRATED, SOCIAL

  public boolean hasRole(String role) {
    return roles != null && roles.contains(role);
  }

  public Long getUserId() {
    return id;
  }

  /**
   * 로그인한 사용자인지 확인
   *
   * @return 로그인한 사용자이면 true, 익명 사용자이면 false
   */
  public boolean isAuthenticated() {
    return this.id != null && !"ANONYMOUS".equals(this.userType);
  }

  /**
   * 익명 사용자인지 확인
   *
   * @return 익명 사용자이면 true, 로그인한 사용자이면 false
   */
  public boolean isAnonymous() {
    return "ANONYMOUS".equals(this.userType) || this.id == null;
  }

  /**
   * 구매자인지 확인
   *
   * @return 구매자이면 true
   */
  public boolean isBuyer() {
    return "BUYER".equals(this.userType);
  }

  /**
   * 판매자인지 확인
   *
   * @return 판매자이면 true
   */
  public boolean isSeller() {
    return "SELLER".equals(this.userType);
  }

  /**
   * 소셜 계정 사용자인지 확인
   *
   * @return 소셜 계정 사용자이면 true
   */
  public boolean isSocialAccount() {
    return "SOCIAL".equals(this.accountType);
  }

  /**
   * 통합 계정 사용자인지 확인
   *
   * @return 통합 계정 사용자이면 true
   */
  public boolean isIntegratedAccount() {
    return "INTEGRATED".equals(this.accountType);
  }
}
