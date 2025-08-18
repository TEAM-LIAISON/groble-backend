package liaison.groble.domain.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatContentOverviewDTO {
  private Long totalContentsCount; // 전체 콘텐츠 개수
  private Long contentId; // 콘텐츠 ID
  private String contentTitle; // 내가 판매하고 있는 콘텐츠 제목
}
