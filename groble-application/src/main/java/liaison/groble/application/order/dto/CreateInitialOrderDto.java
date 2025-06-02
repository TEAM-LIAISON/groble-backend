package liaison.groble.application.order.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateInitialOrderDto {
  private final Long userId;
  private final Long contentId;
  private final List<OrderOptionDto> options;

  @Getter
  @Builder
  public static class OrderOptionDto {
    private final Long optionId;
    private final OptionType optionType;
    private final Integer quantity;
  }

  public enum OptionType {
    COACHING_OPTION,
    DOCUMENT_OPTION
  }
}
