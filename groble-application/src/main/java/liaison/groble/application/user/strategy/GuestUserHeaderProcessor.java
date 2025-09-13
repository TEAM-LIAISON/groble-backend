package liaison.groble.application.user.strategy;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.common.context.UserContext;
import liaison.groble.domain.guest.entity.GuestUser;

/** 비회원(게스트)의 헤더 정보 처리 구현체 */
@Component
public class GuestUserHeaderProcessor extends BaseUserHeaderProcessor {

  private final GuestUserReader guestUserReader;

  public GuestUserHeaderProcessor(GuestUserReader guestUserReader) {
    this.guestUserReader = guestUserReader;
  }

  @Override
  public String getSupportedUserType() {
    return "GUEST";
  }

  @Override
  protected boolean isMemberContext(UserContext userContext) {
    // 게스트 처리기에서는 회원을 처리하지 않음
    return false;
  }

  @Override
  protected boolean isAuthenticatedGuestContext(UserContext userContext) {
    // 게스트이면서 ID가 있는 경우 (토큰으로 식별된 게스트)
    try {
      return userContext.isGuest() && userContext.getId() != null;
    } catch (Exception e) {
      // userContext.getId() 접근 시 오류가 발생하면 인증되지 않은 게스트로 처리
      return false;
    }
  }

  @Override
  protected UserHeaderDTO createMemberResponse(
      UserContext userContext, HttpServletResponse httpResponse) {
    // 게스트 처리기에서는 회원 응답을 생성하지 않음
    // 안전성을 위해 익명 게스트 응답 반환
    return createAnonymousGuestResponse(httpResponse);
  }

  @Override
  protected UserHeaderDTO createAuthenticatedGuestResponse(
      UserContext userContext, HttpServletResponse httpResponse) {
    try {
      // userContext.getId()가 null이 아닌지 다시 한번 확인
      Long guestUserId = userContext.getId();
      if (guestUserId == null) {
        return createAnonymousGuestResponse(httpResponse);
      }

      // 토큰으로 식별된 게스트 사용자 정보 조회
      GuestUser guestUser = guestUserReader.getGuestUserById(guestUserId);

      return UserHeaderDTO.builder()
          .isLogin(false) // 게스트는 로그인 상태가 아님
          .nickname(guestUser.getUsername()) // 게스트 사용자명 사용
          .email(guestUser.getEmail()) // 게스트 이메일 정보
          .profileImageUrl(null) // 게스트는 프로필 이미지 없음
          .canSwitchToSeller(false) // 게스트는 판매자 전환 불가
          .unreadNotificationCount(0) // 게스트는 알림 없음
          .alreadyRegisteredAsSeller(false) // 게스트는 판매자 등록 불가
          .lastUserType(null) // 게스트는 사용자 타입 없음
          .build();
    } catch (Exception e) {
      // 게스트 사용자 조회 실패 시 익명 게스트로 처리
      return createAnonymousGuestResponse(httpResponse);
    }
  }
}
