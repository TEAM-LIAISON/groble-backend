package liaison.groble.api.model.user.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "userType")
@JsonSubTypes({
  @JsonSubTypes.Type(value = BuyerMyPageSummaryResponse.class, name = "BUYER"),
  @JsonSubTypes.Type(value = SellerMyPageSummaryResponse.class, name = "SELLER")
})
public interface MyPageSummaryResponseBase {
  String getNickname();

  String getProfileImageUrl();

  String getUserType();
}
