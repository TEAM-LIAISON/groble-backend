package liaison.groble.api.model.auth.enums;

public enum WithdrawalReasonDto {
  NOT_USING("서비스를 잘 이용하지 않아요"),
  INCONVENIENT("서비스 이용이 불편해요"),
  LACKS_CONTENT("필요한 기능이나 콘텐츠가 없어요"),
  BAD_EXPERIENCE("불편한 경험을 겪었어요"),
  COST_BURDEN("개인 및 비용이 부담돼요"),
  OTHER("기타");

  private final String description;

  WithdrawalReasonDto(String description) {
    this.description = description;
  }
}
