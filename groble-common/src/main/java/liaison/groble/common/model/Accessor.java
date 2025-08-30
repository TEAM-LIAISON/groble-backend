package liaison.groble.common.model;

import java.util.Set;

import liaison.groble.common.context.GuestUserContext;
import liaison.groble.common.context.MemberUserContext;
import liaison.groble.common.context.UserContext;

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
   * 통합 계정 사용자인지 확인
   *
   * @return 통합 계정 사용자이면 true
   */
  public boolean isIntegratedAccount() {
    return "INTEGRATED".equals(this.accountType);
  }

  /**
   * 게스트 사용자인지 확인
   *
   * @return 게스트 사용자이면 true
   */
  public boolean isGuest() {
    return "GUEST".equals(this.userType);
  }

  /**
   * Accessor로부터 UserContext 생성
   *
   * @return 사용자 타입에 따른 UserContext 구현체
   */
  public UserContext toUserContext() {
    if (this.isGuest()) {
      return new GuestUserContext(this.id);
    } else {
      return new MemberUserContext(this.id);
    }
  }

  /**
   * Accessor로부터 UserContext 생성하는 정적 팩토리 메서드
   *
   * @param accessor Accessor 객체
   * @return 사용자 타입에 따른 UserContext 구현체
   */
  public static UserContext toUserContext(Accessor accessor) {
    return accessor.toUserContext();
  }
}
