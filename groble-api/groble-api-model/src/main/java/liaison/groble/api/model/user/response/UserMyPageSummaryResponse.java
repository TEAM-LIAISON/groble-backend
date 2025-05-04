package liaison.groble.api.model.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

// 4. 현재 타입 래퍼 클래스 (기존 코드와의 호환성 유지)
@Getter
@Schema(description = "마이페이지 요약 정보 응답")
public class UserMyPageSummaryResponse {
  @Schema(
      description = "사용자 유형별 응답 객체",
      oneOf = {BuyerMyPageSummaryResponse.class, SellerMyPageSummaryResponse.class})
  private final MyPageSummaryResponseBase data;

  // userType을 외부에서 쉽게 접근할 수 있도록 추가
  @Schema(description = "사용자 유형 코드", example = "BUYER")
  private final String userType;

  public UserMyPageSummaryResponse(MyPageSummaryResponseBase data) {
    this.data = data;
    this.userType = data.getUserType().getCode();
  }
}
