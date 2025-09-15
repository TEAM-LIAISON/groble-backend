package liaison.groble.application.order.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateOrderRequestDTO {
  private final Long contentId;
  private final List<OrderOptionDTO> options;
  private final List<String> couponCodes;
  private final boolean buyerInfoStorageAgreed;

  private CreateOrderRequestDTO(
      Long contentId,
      List<OrderOptionDTO> options,
      List<String> couponCodes,
      boolean buyerInfoStorageAgreed) {
    this.contentId = contentId;
    this.options = options;
    this.couponCodes = couponCodes;
    this.buyerInfoStorageAgreed = buyerInfoStorageAgreed;
  }

  public static CreateOrderRequestDTO of(
      Long contentId,
      List<OrderOptionDTO> options,
      List<String> couponCodes,
      boolean buyerInfoStorageAgreed) {
    return new CreateOrderRequestDTO(contentId, options, couponCodes, buyerInfoStorageAgreed);
  }

  @Getter
  @Builder
  public static class OrderOptionDTO {
    private final Long optionId;
    private final OptionType optionType;
    private final Integer quantity;

    public enum OptionType {
      COACHING_OPTION,
      DOCUMENT_OPTION
    }
  }
}
