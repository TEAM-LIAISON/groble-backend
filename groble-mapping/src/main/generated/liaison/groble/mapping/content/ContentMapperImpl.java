package liaison.groble.mapping.content;

import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.request.draft.ContentDraftRequest;
import liaison.groble.api.model.content.request.register.ContentRegisterRequest;
import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.ContentResponse;
import liaison.groble.api.model.content.response.ContentStatusResponse;
import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.content.dto.ContentDTO;
import liaison.groble.application.content.dto.ContentDetailDTO;
import liaison.groble.application.content.dto.ContentOptionDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-04T21:14:43+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class ContentMapperImpl implements ContentMapper {

  @Override
  public ContentDTO toContentDTO(ContentDraftRequest contentDraftRequest) {
    if (contentDraftRequest == null) {
      return null;
    }

    ContentDTO.ContentDTOBuilder contentDTO = ContentDTO.builder();

    List<ContentOptionDTO> list = mapDraftOptions(contentDraftRequest);
    if (list != null) {
      contentDTO.options(list);
    }
    if (contentDraftRequest.getContentId() != null) {
      contentDTO.contentId(contentDraftRequest.getContentId());
    }
    if (contentDraftRequest.getTitle() != null) {
      contentDTO.title(contentDraftRequest.getTitle());
    }
    if (contentDraftRequest.getContentType() != null) {
      contentDTO.contentType(contentDraftRequest.getContentType());
    }
    if (contentDraftRequest.getCategoryId() != null) {
      contentDTO.categoryId(contentDraftRequest.getCategoryId());
    }
    if (contentDraftRequest.getThumbnailUrl() != null) {
      contentDTO.thumbnailUrl(contentDraftRequest.getThumbnailUrl());
    }
    if (contentDraftRequest.getContentIntroduction() != null) {
      contentDTO.contentIntroduction(contentDraftRequest.getContentIntroduction());
    }
    if (contentDraftRequest.getServiceTarget() != null) {
      contentDTO.serviceTarget(contentDraftRequest.getServiceTarget());
    }
    if (contentDraftRequest.getServiceProcess() != null) {
      contentDTO.serviceProcess(contentDraftRequest.getServiceProcess());
    }
    if (contentDraftRequest.getMakerIntro() != null) {
      contentDTO.makerIntro(contentDraftRequest.getMakerIntro());
    }

    return contentDTO.build();
  }

  @Override
  public ContentDTO toContentDTO(ContentRegisterRequest contentRegisterRequest) {
    if (contentRegisterRequest == null) {
      return null;
    }

    ContentDTO.ContentDTOBuilder contentDTO = ContentDTO.builder();

    List<ContentOptionDTO> list = mapRegisterOptions(contentRegisterRequest);
    if (list != null) {
      contentDTO.options(list);
    }
    if (contentRegisterRequest.getContentId() != null) {
      contentDTO.contentId(contentRegisterRequest.getContentId());
    }
    if (contentRegisterRequest.getTitle() != null) {
      contentDTO.title(contentRegisterRequest.getTitle());
    }
    if (contentRegisterRequest.getContentType() != null) {
      contentDTO.contentType(contentRegisterRequest.getContentType());
    }
    if (contentRegisterRequest.getCategoryId() != null) {
      contentDTO.categoryId(contentRegisterRequest.getCategoryId());
    }
    if (contentRegisterRequest.getThumbnailUrl() != null) {
      contentDTO.thumbnailUrl(contentRegisterRequest.getThumbnailUrl());
    }
    if (contentRegisterRequest.getContentIntroduction() != null) {
      contentDTO.contentIntroduction(contentRegisterRequest.getContentIntroduction());
    }
    if (contentRegisterRequest.getServiceTarget() != null) {
      contentDTO.serviceTarget(contentRegisterRequest.getServiceTarget());
    }
    if (contentRegisterRequest.getServiceProcess() != null) {
      contentDTO.serviceProcess(contentRegisterRequest.getServiceProcess());
    }
    if (contentRegisterRequest.getMakerIntro() != null) {
      contentDTO.makerIntro(contentRegisterRequest.getMakerIntro());
    }

    return contentDTO.build();
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
    if (contentCardDTO.getIsAvailableForSale() != null) {
      contentPreviewCardResponse.isAvailableForSale(contentCardDTO.getIsAvailableForSale());
    }
    if (contentCardDTO.getStatus() != null) {
      contentPreviewCardResponse.status(contentCardDTO.getStatus());
    }
    if (contentCardDTO.getIsDeletable() != null) {
      contentPreviewCardResponse.isDeletable(contentCardDTO.getIsDeletable());
    }

    return contentPreviewCardResponse.build();
  }

  @Override
  public ContentResponse toContentResponse(ContentDTO contentDTO) {
    if (contentDTO == null) {
      return null;
    }

    ContentResponse.ContentResponseBuilder contentResponse = ContentResponse.builder();

    if (contentDTO.getContentId() != null) {
      contentResponse.id(contentDTO.getContentId());
    }
    List<ContentResponse.OptionResponse> list = mapOptionsToResponse(contentDTO.getOptions());
    if (list != null) {
      contentResponse.options(list);
    }
    if (contentDTO.getTitle() != null) {
      contentResponse.title(contentDTO.getTitle());
    }
    if (contentDTO.getContentType() != null) {
      contentResponse.contentType(contentDTO.getContentType());
    }
    if (contentDTO.getCategoryId() != null) {
      contentResponse.categoryId(contentDTO.getCategoryId());
    }
    if (contentDTO.getThumbnailUrl() != null) {
      contentResponse.thumbnailUrl(contentDTO.getThumbnailUrl());
    }
    if (contentDTO.getStatus() != null) {
      contentResponse.status(contentDTO.getStatus());
    }
    if (contentDTO.getContentIntroduction() != null) {
      contentResponse.contentIntroduction(contentDTO.getContentIntroduction());
    }
    if (contentDTO.getServiceTarget() != null) {
      contentResponse.serviceTarget(contentDTO.getServiceTarget());
    }
    if (contentDTO.getServiceProcess() != null) {
      contentResponse.serviceProcess(contentDTO.getServiceProcess());
    }
    if (contentDTO.getMakerIntro() != null) {
      contentResponse.makerIntro(contentDTO.getMakerIntro());
    }

    return contentResponse.build();
  }

  @Override
  public ContentStatusResponse toContentStatusResponse(ContentDTO contentDTO) {
    if (contentDTO == null) {
      return null;
    }

    ContentStatusResponse.ContentStatusResponseBuilder contentStatusResponse =
        ContentStatusResponse.builder();

    if (contentDTO.getContentId() != null) {
      contentStatusResponse.contentId(contentDTO.getContentId());
    }
    if (contentDTO.getStatus() != null) {
      contentStatusResponse.status(contentDTO.getStatus());
    }

    return contentStatusResponse.build();
  }

  @Override
  public ContentDetailResponse toContentDetailResponse(
      ContentDetailDTO contentDetailDTO, ContactInfoResponse contactInfoResponse) {
    if (contentDetailDTO == null && contactInfoResponse == null) {
      return null;
    }

    ContentDetailResponse.ContentDetailResponseBuilder contentDetailResponse =
        ContentDetailResponse.builder();

    if (contentDetailDTO != null) {
      List<?> list = mapOptionsToBaseOptionResponse(contentDetailDTO.getOptions());
      if (list != null) {
        contentDetailResponse.options(list);
      }
      if (contentDetailDTO.getContentId() != null) {
        contentDetailResponse.contentId(contentDetailDTO.getContentId());
      }
      if (contentDetailDTO.getStatus() != null) {
        contentDetailResponse.status(contentDetailDTO.getStatus());
      }
      if (contentDetailDTO.getThumbnailUrl() != null) {
        contentDetailResponse.thumbnailUrl(contentDetailDTO.getThumbnailUrl());
      }
      if (contentDetailDTO.getContentType() != null) {
        contentDetailResponse.contentType(contentDetailDTO.getContentType());
      }
      if (contentDetailDTO.getCategoryId() != null) {
        contentDetailResponse.categoryId(contentDetailDTO.getCategoryId());
      }
      if (contentDetailDTO.getTitle() != null) {
        contentDetailResponse.title(contentDetailDTO.getTitle());
      }
      if (contentDetailDTO.getSellerProfileImageUrl() != null) {
        contentDetailResponse.sellerProfileImageUrl(contentDetailDTO.getSellerProfileImageUrl());
      }
      if (contentDetailDTO.getSellerName() != null) {
        contentDetailResponse.sellerName(contentDetailDTO.getSellerName());
      }
      if (contentDetailDTO.getLowestPrice() != null) {
        contentDetailResponse.lowestPrice(contentDetailDTO.getLowestPrice());
      }
      if (contentDetailDTO.getContentIntroduction() != null) {
        contentDetailResponse.contentIntroduction(contentDetailDTO.getContentIntroduction());
      }
      if (contentDetailDTO.getServiceTarget() != null) {
        contentDetailResponse.serviceTarget(contentDetailDTO.getServiceTarget());
      }
      if (contentDetailDTO.getServiceProcess() != null) {
        contentDetailResponse.serviceProcess(contentDetailDTO.getServiceProcess());
      }
      if (contentDetailDTO.getMakerIntro() != null) {
        contentDetailResponse.makerIntro(contentDetailDTO.getMakerIntro());
      }
    }
    if (contactInfoResponse != null) {
      contentDetailResponse.contactInfo(contactInfoResponse);
    }
    contentDetailResponse.priceOptionLength(
        contentDetailDTO.getOptions() != null ? contentDetailDTO.getOptions().size() : 0);

    return contentDetailResponse.build();
  }
}
