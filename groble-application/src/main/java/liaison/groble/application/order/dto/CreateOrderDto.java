package liaison.groble.application.order.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateOrderDto {
  private final String email;
  private final String phoneNumber;

  private final Long contentId;
  private final List<OrderOptionDto> options;
  private final List<String> couponCodes;

  @Getter
  @Builder
  public static class OrderOptionDto {
    private final Long optionId;
    private final CreateOrderDto.OptionType optionType;
    private final Integer quantity;
  }

  public enum OptionType {
    COACHING_OPTION,
    DOCUMENT_OPTION
  }
}
