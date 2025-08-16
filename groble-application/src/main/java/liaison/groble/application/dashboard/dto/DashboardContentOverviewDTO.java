package liaison.groble.application.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardContentOverviewDTO {
  private Long totalContentsCount; // 전체 콘텐츠 개수
  private Long contentId; // 콘텐츠 ID
  private String contentTitle; // 내가 판매하고 있는 콘텐츠 제목
}
