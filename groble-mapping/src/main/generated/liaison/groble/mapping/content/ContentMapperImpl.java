package liaison.groble.mapping.content;

import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.content.request.draft.ContentDraftRequest;
import liaison.groble.api.model.content.request.register.ContentRegisterRequest;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.content.response.ContentResponse;
import liaison.groble.api.model.content.response.ContentStatusResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.content.dto.ContentDTO;
import liaison.groble.application.content.dto.ContentOptionDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-28T01:42:29+0900",
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
    if (contentCardDTO.getStatus() != null) {
      contentPreviewCardResponse.status(contentCardDTO.getStatus());
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
}
