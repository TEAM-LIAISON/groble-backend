package liaison.groble.application.sell.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentSellDetailDTO {
  private Long purchaseId; // Purchase.id
  private String contentTitle; // Content.title
  private LocalDateTime purchasedAt; // Purchase.purchasedAt
  private String purchaserNickname; // Purchase.user.userProfile.nickname
  private String purchaserEmail; // Purchase.user.userProfile.email
  private String purchaserPhoneNumber; // Purchase.user.userProfile.phoneNumber
  private String selectedOptionName; // Purchase.selectedOptionName
  private BigDecimal finalPrice; // Purchase.finalPrice
}
