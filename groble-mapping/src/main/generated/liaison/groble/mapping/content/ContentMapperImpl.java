package liaison.groble.mapping.content;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.application.content.dto.ContentCardDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-23T16:53:12+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class ContentMapperImpl implements ContentMapper {

  @Override
  public ContentPreviewCardResponse toContentPreviewCardResponse(ContentCardDTO contentCardDto) {
    if (contentCardDto == null) {
      return null;
    }

    ContentPreviewCardResponse.ContentPreviewCardResponseBuilder contentPreviewCardResponse =
        ContentPreviewCardResponse.builder();

    if (contentCardDto.getContentId() != null) {
      contentPreviewCardResponse.contentId(contentCardDto.getContentId());
    }
    if (contentCardDto.getCreatedAt() != null) {
      contentPreviewCardResponse.createdAt(contentCardDto.getCreatedAt());
    }
    if (contentCardDto.getTitle() != null) {
      contentPreviewCardResponse.title(contentCardDto.getTitle());
    }
    if (contentCardDto.getThumbnailUrl() != null) {
      contentPreviewCardResponse.thumbnailUrl(contentCardDto.getThumbnailUrl());
    }
    if (contentCardDto.getSellerName() != null) {
      contentPreviewCardResponse.sellerName(contentCardDto.getSellerName());
    }
    if (contentCardDto.getLowestPrice() != null) {
      contentPreviewCardResponse.lowestPrice(contentCardDto.getLowestPrice());
    }
    contentPreviewCardResponse.priceOptionLength(contentCardDto.getPriceOptionLength());
    if (contentCardDto.getStatus() != null) {
      contentPreviewCardResponse.status(contentCardDto.getStatus());
    }

    return contentPreviewCardResponse.build();
  }
}
