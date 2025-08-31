package liaison.groble.common.context;

import lombok.RequiredArgsConstructor;

/** 비회원(게스트) 사용자 컨텍스트 */
@RequiredArgsConstructor
public class GuestUserContext implements UserContext {

  private final Long guestUserId;

  @Override
  public Long getId() {
    return guestUserId;
  }

  @Override
  public String getUserType() {
    return "GUEST";
  }

  @Override
  public boolean isMember() {
    return false;
  }

  @Override
  public boolean isGuest() {
    return true;
  }

  @Override
  public boolean isAuthenticated() {
    return guestUserId != null;
  }
}
