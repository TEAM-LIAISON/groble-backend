package liaison.groble.mapping.content;

import java.util.List;
import javax.annotation.processing.Generated;
import liaison.groble.api.model.content.request.draft.ContentDraftRequest;
import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.content.dto.ContentDTO;
import liaison.groble.application.content.dto.ContentOptionDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-27T14:48:48+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)"
)
@Component
public class ContentMapperImpl implements ContentMapper {

    @Override
    public ContentDTO toContentDTO(ContentDraftRequest contentDraftRequest) {
        if ( contentDraftRequest == null ) {
            return null;
        }

        ContentDTO.ContentDTOBuilder contentDTO = ContentDTO.builder();

        List<ContentOptionDTO> list = mapOptions( contentDraftRequest );
        if ( list != null ) {
            contentDTO.options( list );
        }
        if ( contentDraftRequest.getContentId() != null ) {
            contentDTO.contentId( contentDraftRequest.getContentId() );
        }
        if ( contentDraftRequest.getTitle() != null ) {
            contentDTO.title( contentDraftRequest.getTitle() );
        }
        if ( contentDraftRequest.getContentType() != null ) {
            contentDTO.contentType( contentDraftRequest.getContentType() );
        }
        if ( contentDraftRequest.getCategoryId() != null ) {
            contentDTO.categoryId( contentDraftRequest.getCategoryId() );
        }
        if ( contentDraftRequest.getThumbnailUrl() != null ) {
            contentDTO.thumbnailUrl( contentDraftRequest.getThumbnailUrl() );
        }
        if ( contentDraftRequest.getContentIntroduction() != null ) {
            contentDTO.contentIntroduction( contentDraftRequest.getContentIntroduction() );
        }
        if ( contentDraftRequest.getServiceTarget() != null ) {
            contentDTO.serviceTarget( contentDraftRequest.getServiceTarget() );
        }
        if ( contentDraftRequest.getServiceProcess() != null ) {
            contentDTO.serviceProcess( contentDraftRequest.getServiceProcess() );
        }
        if ( contentDraftRequest.getMakerIntro() != null ) {
            contentDTO.makerIntro( contentDraftRequest.getMakerIntro() );
        }

        return contentDTO.build();
    }

    @Override
    public ContentPreviewCardResponse toContentPreviewCardResponse(ContentCardDTO contentCardDTO) {
        if ( contentCardDTO == null ) {
            return null;
        }

        ContentPreviewCardResponse.ContentPreviewCardResponseBuilder contentPreviewCardResponse = ContentPreviewCardResponse.builder();

        if ( contentCardDTO.getContentId() != null ) {
            contentPreviewCardResponse.contentId( contentCardDTO.getContentId() );
        }
        if ( contentCardDTO.getCreatedAt() != null ) {
            contentPreviewCardResponse.createdAt( contentCardDTO.getCreatedAt() );
        }
        if ( contentCardDTO.getTitle() != null ) {
            contentPreviewCardResponse.title( contentCardDTO.getTitle() );
        }
        if ( contentCardDTO.getThumbnailUrl() != null ) {
            contentPreviewCardResponse.thumbnailUrl( contentCardDTO.getThumbnailUrl() );
        }
        if ( contentCardDTO.getSellerName() != null ) {
            contentPreviewCardResponse.sellerName( contentCardDTO.getSellerName() );
        }
        if ( contentCardDTO.getLowestPrice() != null ) {
            contentPreviewCardResponse.lowestPrice( contentCardDTO.getLowestPrice() );
        }
        contentPreviewCardResponse.priceOptionLength( contentCardDTO.getPriceOptionLength() );
        if ( contentCardDTO.getStatus() != null ) {
            contentPreviewCardResponse.status( contentCardDTO.getStatus() );
        }

        return contentPreviewCardResponse.build();
    }
}
