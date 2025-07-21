package liaison.groble.mapping.common;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.config.GrobleMapperConfig;
import liaison.groble.mapping.content.ContentReplyMapper;
import liaison.groble.mapping.sell.SellMapper;

@Mapper(
    config = GrobleMapperConfig.class,
    uses = {ContentReplyMapper.class, SellMapper.class})
public interface PageResponseMapper {
  default <S, T> PageResponse<T> toPageResponse(
      PageResponse<S> sourcePage, Function<S, T> converter) {
    if (sourcePage == null) {
      return null;
    }

    List<T> convertedItems =
        sourcePage.getItems().stream().map(converter).collect(Collectors.toList());

    return PageResponse.<T>builder()
        .items(convertedItems)
        .pageInfo(sourcePage.getPageInfo())
        .meta(sourcePage.getMeta())
        .build();
  }
}
