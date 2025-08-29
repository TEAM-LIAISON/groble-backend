package liaison.groble.mapping.guest;

import org.mapstruct.Mapper;

import liaison.groble.api.model.guest.request.GuestAuthRequest;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface GuestAuthMapper {
  GuestAuthDTO toGuestAuthDTO(GuestAuthRequest guestAuthRequest);
}
