package liaison.groble.common.factory;

import liaison.groble.common.context.GuestUserContext;
import liaison.groble.common.context.MemberUserContext;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.model.Accessor;

/** UserContext 생성을 담당하는 팩토리 클래스 중복 로직을 방지하고 일관된 UserContext 생성을 보장합니다. */
public final class UserContextFactory {

  private UserContextFactory() {
    // 유틸리티 클래스는 인스턴스 생성 방지
  }

  /**
   * Accessor로부터 UserContext 생성
   *
   * @param accessor Accessor 객체
   * @return 적절한 UserContext 구현체
   */
  public static UserContext from(Accessor accessor) {
    if (accessor.isGuest()) {
      return new GuestUserContext(accessor.getId());
    } else {
      return new MemberUserContext(accessor.getId());
    }
  }

  /**
   * userId와 guestUserId로부터 UserContext 생성
   *
   * @param userId 회원 사용자 ID (nullable)
   * @param guestUserId 비회원 사용자 ID (nullable)
   * @return 적절한 UserContext 구현체
   * @throws IllegalArgumentException 둘 다 null이거나 둘 다 값이 있는 경우
   */
  public static UserContext from(Long userId, Long guestUserId) {
    if (userId != null && guestUserId != null) {
      throw new IllegalArgumentException("회원 ID와 비회원 ID가 동시에 제공될 수 없습니다.");
    }

    if (userId != null) {
      return new MemberUserContext(userId);
    } else if (guestUserId != null) {
      return new GuestUserContext(guestUserId);
    } else {
      throw new IllegalArgumentException("회원 ID 또는 비회원 ID 중 하나는 반드시 제공되어야 합니다.");
    }
  }

  /**
   * 회원 사용자 컨텍스트 생성
   *
   * @param userId 회원 사용자 ID
   * @return MemberUserContext
   */
  public static UserContext createMemberContext(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("회원 ID는 null일 수 없습니다.");
    }
    return new MemberUserContext(userId);
  }

  /**
   * 비회원 사용자 컨텍스트 생성
   *
   * @param guestUserId 비회원 사용자 ID
   * @return GuestUserContext
   */
  public static UserContext createGuestContext(Long guestUserId) {
    if (guestUserId == null) {
      throw new IllegalArgumentException("비회원 ID는 null일 수 없습니다.");
    }
    return new GuestUserContext(guestUserId);
  }
}
