package liaison.groble.application.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentTotalViewStatsDTO {
  private Long contentId;
  private String contentTitle;
  private Long totalViews;
}
