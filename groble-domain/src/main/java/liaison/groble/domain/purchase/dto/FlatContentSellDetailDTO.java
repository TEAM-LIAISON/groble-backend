package liaison.groble.domain.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatContentSellDetailDTO {
  private Long purchaseId; // Purchase.id
  private String contentTitle; // Content.title
  private LocalDateTime purchasedAt; // Purchase.purchasedAt
  private String purchaserNickname; // Purchase.user.userProfile.nickname
  private String purchaserEmail; // Purchase.user.userProfile.email
  private String purchaserPhoneNumber; // Purchase.user.userProfile.phoneNumber
  private String selectedOptionName; // Purchase.selectedOptionName
  private BigDecimal finalPrice; // Purchase.finalPrice
  private String paymentType; // Content.paymentType
  private Integer subscriptionRound; // 정기결제 회차 (null 가능)
}
