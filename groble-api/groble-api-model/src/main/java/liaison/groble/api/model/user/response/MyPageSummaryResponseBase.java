package liaison.groble.api.model.user.response;

import liaison.groble.common.response.EnumResponse;

public interface MyPageSummaryResponseBase {
  String getNickname();

  String getProfileImageUrl();

  EnumResponse getUserType();
}
