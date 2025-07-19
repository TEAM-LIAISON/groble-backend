package liaison.groble.domain.notification.entity.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellDetails {
  // 판매된 콘텐츠 ID
  private Long contentId;
}
