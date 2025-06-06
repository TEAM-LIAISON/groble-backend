package liaison.groble.application.order.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateOrderDto {
  private final Long contentId;
  private final List<OrderOptionDto> options;
  private final List<String> couponCodes;

  private CreateOrderDto(Long contentId, List<OrderOptionDto> options, List<String> couponCodes) {
    this.contentId = contentId;
    this.options = options;
    this.couponCodes = couponCodes;
  }

  public static CreateOrderDto of(
      Long contentId, List<OrderOptionDto> options, List<String> couponCodes) {
    return new CreateOrderDto(contentId, options, couponCodes);
  }

  @Getter
  @Builder
  public static class OrderOptionDto {
    private final Long optionId;
    private final OptionType optionType;
    private final Integer quantity;

    public enum OptionType {
      COACHING_OPTION,
      DOCUMENT_OPTION
    }
  }
}
