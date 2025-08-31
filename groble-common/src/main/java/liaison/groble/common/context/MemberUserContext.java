package liaison.groble.common.context;

import lombok.RequiredArgsConstructor;

/** 회원 사용자 컨텍스트 */
@RequiredArgsConstructor
public class MemberUserContext implements UserContext {

  private final Long memberId;

  @Override
  public Long getId() {
    return memberId;
  }

  @Override
  public String getUserType() {
    return "MEMBER";
  }

  @Override
  public boolean isMember() {
    return true;
  }

  @Override
  public boolean isGuest() {
    return false;
  }

  @Override
  public boolean isAuthenticated() {
    return memberId != null;
  }
}
