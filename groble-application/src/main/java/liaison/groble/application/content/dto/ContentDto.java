package liaison.groble.application.content.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDto {
  private Long contentId;
  private String title;
  private String contentType;
  private Long categoryId;
  private String thumbnailUrl;
  private String status;
  private List<ContentOptionDto> options;
}
