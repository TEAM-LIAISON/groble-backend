package liaison.groble.domain.notification.entity.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetails {
  private Long contentId;
  private Long reviewId;
}
