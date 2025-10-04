package liaison.groble.application.content.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDTO {
  private Long contentId;
  private String title;
  private String contentType;
  private String categoryId;
  private String thumbnailUrl;
  private String status;
  private Boolean isSearchExposed;
  private List<ContentOptionDTO> options;
  private String contentIntroduction;
  private String serviceTarget;
  private String serviceProcess;
  private String makerIntro;
}
