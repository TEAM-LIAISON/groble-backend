package liaison.groble.application.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserPaymentMethodDTO {
  private final boolean hasPaymentMethod;
  private final String cardName;
  private final String cardNumberSuffix;
  private final boolean hasActiveSubscription;

  public static UserPaymentMethodDTO empty() {
    return empty(false);
  }

  public static UserPaymentMethodDTO empty(boolean hasActiveSubscription) {
    return UserPaymentMethodDTO.builder()
        .hasPaymentMethod(false)
        .cardName(null)
        .cardNumberSuffix(null)
        .hasActiveSubscription(hasActiveSubscription)
        .build();
  }
}
