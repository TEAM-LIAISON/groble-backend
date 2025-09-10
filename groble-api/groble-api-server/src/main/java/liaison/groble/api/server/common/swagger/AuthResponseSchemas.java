package liaison.groble.api.server.common.swagger;

import liaison.groble.api.model.guest.response.GuestAuthCodeResponse;
import liaison.groble.api.model.guest.response.UpdateGuestUserInfoResponse;
import liaison.groble.api.model.guest.response.VerifyAuthCodeResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 인증 관련 API 응답 스키마 클래스
 *
 * <p>Swagger 문서화를 위한 인증 및 게스트 사용자 전용 응답 스키마들을 정의합니다.
 */
public final class AuthResponseSchemas {

  private AuthResponseSchemas() {}

  /** 비회원 인증 코드 요청 응답 스키마 */
  @Schema(description = "비회원 인증 요청 응답")
  public static class ApiGuestAuthResponse extends GrobleResponse<GuestAuthCodeResponse> {

    @Override
    @Schema(description = "비회원 인증 코드 발송 요청 응답", implementation = GuestAuthCodeResponse.class)
    public GuestAuthCodeResponse getData() {
      return super.getData();
    }
  }

  /** 비회원 인증 코드 검증 응답 스키마 */
  @Schema(description = "비회원 인증 검증 응답")
  public static class ApiVerifyGuestAuthResponse extends GrobleResponse<VerifyAuthCodeResponse> {

    @Override
    @Schema(description = "비회원 인증 코드 검증 결과 응답", implementation = VerifyAuthCodeResponse.class)
    public VerifyAuthCodeResponse getData() {
      return super.getData();
    }
  }

  /** 비회원 사용자 정보 업데이트 응답 스키마 */
  @Schema(description = "비회원 사용자 정보 업데이트 응답")
  public static class ApiUpdateGuestUserInfoResponse
      extends GrobleResponse<UpdateGuestUserInfoResponse> {

    @Override
    @Schema(
        description = "비회원 사용자 정보 업데이트 및 정식 토큰 발급 응답",
        implementation = UpdateGuestUserInfoResponse.class)
    public UpdateGuestUserInfoResponse getData() {
      return super.getData();
    }
  }
}
