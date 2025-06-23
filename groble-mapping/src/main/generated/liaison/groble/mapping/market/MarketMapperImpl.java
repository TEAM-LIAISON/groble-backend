package liaison.groble.mapping.market;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-23T20:11:12+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class MarketMapperImpl implements MarketMapper {

  @Override
  public MakerIntroSectionResponse toMakerIntroSectionResponse(
      MarketIntroSectionDTO marketIntroSectionDTO) {
    if (marketIntroSectionDTO == null) {
      return null;
    }

    MakerIntroSectionResponse.MakerIntroSectionResponseBuilder makerIntroSectionResponse =
        MakerIntroSectionResponse.builder();

    if (marketIntroSectionDTO.getProfileImageUrl() != null) {
      makerIntroSectionResponse.profileImageUrl(marketIntroSectionDTO.getProfileImageUrl());
    }
    if (marketIntroSectionDTO.getMarketName() != null) {
      makerIntroSectionResponse.marketName(marketIntroSectionDTO.getMarketName());
    }
    if (marketIntroSectionDTO.getVerificationStatus() != null) {
      makerIntroSectionResponse.verificationStatus(marketIntroSectionDTO.getVerificationStatus());
    }
    if (marketIntroSectionDTO.getContactInfo() != null) {
      makerIntroSectionResponse.contactInfo(
          toContactInfoResponse(marketIntroSectionDTO.getContactInfo()));
    }
    if (marketIntroSectionDTO.getRepresentativeContent() != null) {
      makerIntroSectionResponse.representativeContent(
          flatContentPreviewDTOToContentPreviewCardResponse(
              marketIntroSectionDTO.getRepresentativeContent()));
    }

    return makerIntroSectionResponse.build();
  }

  @Override
  public ContactInfoResponse toContactInfoResponse(ContactInfoDTO contactInfoDTO) {
    if (contactInfoDTO == null) {
      return null;
    }

    ContactInfoResponse.ContactInfoResponseBuilder contactInfoResponse =
        ContactInfoResponse.builder();

    if (contactInfoDTO.getInstagram() != null) {
      contactInfoResponse.instagram(contactInfoDTO.getInstagram());
    }
    if (contactInfoDTO.getEmail() != null) {
      contactInfoResponse.email(contactInfoDTO.getEmail());
    }
    if (contactInfoDTO.getOpenChat() != null) {
      contactInfoResponse.openChat(contactInfoDTO.getOpenChat());
    }
    if (contactInfoDTO.getEtc() != null) {
      contactInfoResponse.etc(contactInfoDTO.getEtc());
    }

    return contactInfoResponse.build();
  }

  @Override
  public ContentPreviewCardResponse toContentPreviewCardResponse(ContentCardDTO contentCardDTO) {
    if (contentCardDTO == null) {
      return null;
    }

    ContentPreviewCardResponse.ContentPreviewCardResponseBuilder contentPreviewCardResponse =
        ContentPreviewCardResponse.builder();

    if (contentCardDTO.getContentId() != null) {
      contentPreviewCardResponse.contentId(contentCardDTO.getContentId());
    }
    if (contentCardDTO.getCreatedAt() != null) {
      contentPreviewCardResponse.createdAt(contentCardDTO.getCreatedAt());
    }
    if (contentCardDTO.getTitle() != null) {
      contentPreviewCardResponse.title(contentCardDTO.getTitle());
    }
    if (contentCardDTO.getThumbnailUrl() != null) {
      contentPreviewCardResponse.thumbnailUrl(contentCardDTO.getThumbnailUrl());
    }
    if (contentCardDTO.getSellerName() != null) {
      contentPreviewCardResponse.sellerName(contentCardDTO.getSellerName());
    }
    if (contentCardDTO.getLowestPrice() != null) {
      contentPreviewCardResponse.lowestPrice(contentCardDTO.getLowestPrice());
    }
    contentPreviewCardResponse.priceOptionLength(contentCardDTO.getPriceOptionLength());
    if (contentCardDTO.getStatus() != null) {
      contentPreviewCardResponse.status(contentCardDTO.getStatus());
    }

    return contentPreviewCardResponse.build();
  }

  @Override
  public List<ContentPreviewCardResponse> toContentPreviewCardResponseList(
      List<ContentCardDTO> contentCardDTOList) {
    if (contentCardDTOList == null) {
      return null;
    }

    List<ContentPreviewCardResponse> list =
        new ArrayList<ContentPreviewCardResponse>(contentCardDTOList.size());
    for (ContentCardDTO contentCardDTO : contentCardDTOList) {
      list.add(toContentPreviewCardResponse(contentCardDTO));
    }

    return list;
  }

  @Override
  public PageResponse<ContentPreviewCardResponse> toContentPreviewCardResponsePage(
      PageResponse<ContentCardDTO> dtoPageResponse) {
    if (dtoPageResponse == null) {
      return null;
    }

    PageResponse<ContentPreviewCardResponse> pageResponse =
        new PageResponse<ContentPreviewCardResponse>();

    if (pageResponse.getItems() != null) {
      List<ContentPreviewCardResponse> list =
          toContentPreviewCardResponseList(dtoPageResponse.getItems());
      if (list != null) {
        pageResponse.getItems().addAll(list);
      }
    }

    return pageResponse;
  }

  protected ContentPreviewCardResponse flatContentPreviewDTOToContentPreviewCardResponse(
      FlatContentPreviewDTO flatContentPreviewDTO) {
    if (flatContentPreviewDTO == null) {
      return null;
    }

    ContentPreviewCardResponse.ContentPreviewCardResponseBuilder contentPreviewCardResponse =
        ContentPreviewCardResponse.builder();

    if (flatContentPreviewDTO.getContentId() != null) {
      contentPreviewCardResponse.contentId(flatContentPreviewDTO.getContentId());
    }
    if (flatContentPreviewDTO.getCreatedAt() != null) {
      contentPreviewCardResponse.createdAt(flatContentPreviewDTO.getCreatedAt());
    }
    if (flatContentPreviewDTO.getTitle() != null) {
      contentPreviewCardResponse.title(flatContentPreviewDTO.getTitle());
    }
    if (flatContentPreviewDTO.getThumbnailUrl() != null) {
      contentPreviewCardResponse.thumbnailUrl(flatContentPreviewDTO.getThumbnailUrl());
    }
    if (flatContentPreviewDTO.getSellerName() != null) {
      contentPreviewCardResponse.sellerName(flatContentPreviewDTO.getSellerName());
    }
    if (flatContentPreviewDTO.getLowestPrice() != null) {
      contentPreviewCardResponse.lowestPrice(flatContentPreviewDTO.getLowestPrice());
    }
    contentPreviewCardResponse.priceOptionLength(flatContentPreviewDTO.getPriceOptionLength());
    if (flatContentPreviewDTO.getStatus() != null) {
      contentPreviewCardResponse.status(flatContentPreviewDTO.getStatus());
    }

    return contentPreviewCardResponse.build();
  }
}
