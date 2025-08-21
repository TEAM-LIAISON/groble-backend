package liaison.groble.mapping.content;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.dashboard.request.referrer.ReferrerRequest;
import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;
import liaison.groble.mapping.dashboard.ReferrerMapper;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-21T12:05:29+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class ReferrerMapperImpl implements ReferrerMapper {

  @Override
  public ReferrerDTO toContentReferrerDTO(ReferrerRequest referrerRequest) {
    if (referrerRequest == null) {
      return null;
    }

    ReferrerDTO.ContentReferrerDTOBuilder contentReferrerDTO = ReferrerDTO.builder();

    if (referrerRequest.getPageUrl() != null) {
      contentReferrerDTO.pageUrl(referrerRequest.getPageUrl());
    }
    if (referrerRequest.getReferrerUrl() != null) {
      contentReferrerDTO.referrerUrl(referrerRequest.getReferrerUrl());
    }
    if (referrerRequest.getUtmSource() != null) {
      contentReferrerDTO.utmSource(referrerRequest.getUtmSource());
    }
    if (referrerRequest.getUtmMedium() != null) {
      contentReferrerDTO.utmMedium(referrerRequest.getUtmMedium());
    }
    if (referrerRequest.getUtmCampaign() != null) {
      contentReferrerDTO.utmCampaign(referrerRequest.getUtmCampaign());
    }
    if (referrerRequest.getUtmContent() != null) {
      contentReferrerDTO.utmContent(referrerRequest.getUtmContent());
    }
    if (referrerRequest.getUtmTerm() != null) {
      contentReferrerDTO.utmTerm(referrerRequest.getUtmTerm());
    }

    return contentReferrerDTO.build();
  }
}
