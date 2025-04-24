package liaison.groble.api.server.gig.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.gig.request.GigDraftRequest;
import liaison.groble.application.gig.dto.GigDraftDto;

@Component
public class GigDtoMapper {

  public GigDraftDto toServiceGigDraftDto(GigDraftRequest request) {
    return GigDraftDto.builder().build();
  }
}
