package liaison.groble.application.purchase.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchasedContentSellerContactResponse {
  private String sellerContactType;
  private String sellerContactUrl;
}
