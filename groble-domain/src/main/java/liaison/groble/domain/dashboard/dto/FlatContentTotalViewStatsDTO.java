package liaison.groble.domain.dashboard.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatContentTotalViewStatsDTO {
  private Long contentId;
  private String contentTitle;
  private Long totalViews;
  private LocalDateTime contentCreatedAt;
}
