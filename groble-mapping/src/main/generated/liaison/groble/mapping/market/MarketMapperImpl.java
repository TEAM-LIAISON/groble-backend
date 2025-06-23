package liaison.groble.mapping.market;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-23T16:53:12+0900",
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
          contentCardDTOToContentPreviewCardResponse(
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

  protected ContentPreviewCardResponse contentCardDTOToContentPreviewCardResponse(
      ContentCardDTO contentCardDTO) {
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
}
