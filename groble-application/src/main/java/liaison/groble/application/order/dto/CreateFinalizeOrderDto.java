package liaison.groble.application.order.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateFinalizeOrderDto {
  private final Long userId;
  private final Long contentId;
  private final String merchantUid;
  private final List<CreateFinalizeOrderDto.OrderOptionDto> options;
  private final List<String> couponCodes;

  @Getter
  @Builder
  public static class OrderOptionDto {
    private final Long optionId;
    private final CreateFinalizeOrderDto.OptionType optionType;
    private final Integer quantity;
  }

  public enum OptionType {
    COACHING_OPTION,
    DOCUMENT_OPTION
  }
}
