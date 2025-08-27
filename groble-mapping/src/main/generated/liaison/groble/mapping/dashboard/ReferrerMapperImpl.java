package liaison.groble.mapping.dashboard;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.dashboard.request.referrer.ReferrerRequest;
import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-27T18:38:53+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class ReferrerMapperImpl implements ReferrerMapper {

  @Override
  public ReferrerDTO toContentReferrerDTO(ReferrerRequest referrerRequest) {
    if (referrerRequest == null) {
      return null;
    }

    ReferrerDTO.ReferrerDTOBuilder referrerDTO = ReferrerDTO.builder();

    if (referrerRequest.getPageUrl() != null) {
      referrerDTO.pageUrl(referrerRequest.getPageUrl());
    }
    if (referrerRequest.getReferrerUrl() != null) {
      referrerDTO.referrerUrl(referrerRequest.getReferrerUrl());
    }
    if (referrerRequest.getUtmSource() != null) {
      referrerDTO.utmSource(referrerRequest.getUtmSource());
    }
    if (referrerRequest.getUtmMedium() != null) {
      referrerDTO.utmMedium(referrerRequest.getUtmMedium());
    }
    if (referrerRequest.getUtmCampaign() != null) {
      referrerDTO.utmCampaign(referrerRequest.getUtmCampaign());
    }
    if (referrerRequest.getUtmContent() != null) {
      referrerDTO.utmContent(referrerRequest.getUtmContent());
    }
    if (referrerRequest.getUtmTerm() != null) {
      referrerDTO.utmTerm(referrerRequest.getUtmTerm());
    }

    return referrerDTO.build();
  }
}
