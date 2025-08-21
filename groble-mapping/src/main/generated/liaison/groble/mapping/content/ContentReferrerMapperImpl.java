package liaison.groble.mapping.content;

import javax.annotation.processing.Generated;
import liaison.groble.api.model.content.request.referrer.ContentReferrerRequest;
import liaison.groble.application.content.dto.referrer.ContentReferrerDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-21T12:05:29+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)"
)
@Component
public class ContentReferrerMapperImpl implements ContentReferrerMapper {

    @Override
    public ContentReferrerDTO toContentReferrerDTO(ContentReferrerRequest contentReferrerRequest) {
        if ( contentReferrerRequest == null ) {
            return null;
        }

        ContentReferrerDTO.ContentReferrerDTOBuilder contentReferrerDTO = ContentReferrerDTO.builder();

        if ( contentReferrerRequest.getPageUrl() != null ) {
            contentReferrerDTO.pageUrl( contentReferrerRequest.getPageUrl() );
        }
        if ( contentReferrerRequest.getReferrerUrl() != null ) {
            contentReferrerDTO.referrerUrl( contentReferrerRequest.getReferrerUrl() );
        }
        if ( contentReferrerRequest.getUtmSource() != null ) {
            contentReferrerDTO.utmSource( contentReferrerRequest.getUtmSource() );
        }
        if ( contentReferrerRequest.getUtmMedium() != null ) {
            contentReferrerDTO.utmMedium( contentReferrerRequest.getUtmMedium() );
        }
        if ( contentReferrerRequest.getUtmCampaign() != null ) {
            contentReferrerDTO.utmCampaign( contentReferrerRequest.getUtmCampaign() );
        }
        if ( contentReferrerRequest.getUtmContent() != null ) {
            contentReferrerDTO.utmContent( contentReferrerRequest.getUtmContent() );
        }
        if ( contentReferrerRequest.getUtmTerm() != null ) {
            contentReferrerDTO.utmTerm( contentReferrerRequest.getUtmTerm() );
        }

        return contentReferrerDTO.build();
    }
}
