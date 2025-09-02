package liaison.groble.mapping.guest;

import org.mapstruct.Mapper;

import liaison.groble.api.model.guest.request.GuestAuthCodeRequest;
import liaison.groble.api.model.guest.request.GuestAuthVerifyRequest;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.dto.GuestAuthVerifyDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface GuestAuthMapper {
  GuestAuthDTO toGuestAuthDTO(GuestAuthCodeRequest guestAuthCodeRequest);

  GuestAuthVerifyDTO toGuestAuthVerifyDTO(GuestAuthVerifyRequest guestAuthVerifyRequest);
}
