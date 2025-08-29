package liaison.groble.mapping.guest;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.guest.request.GuestAuthRequest;
import liaison.groble.application.guest.dto.GuestAuthDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-29T16:29:01+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class GuestAuthMapperImpl implements GuestAuthMapper {

  @Override
  public GuestAuthDTO toGuestAuthDTO(GuestAuthRequest guestAuthRequest) {
    if (guestAuthRequest == null) {
      return null;
    }

    GuestAuthDTO.GuestAuthDTOBuilder guestAuthDTO = GuestAuthDTO.builder();

    if (guestAuthRequest.getUsername() != null) {
      guestAuthDTO.username(guestAuthRequest.getUsername());
    }
    if (guestAuthRequest.getPhoneNumber() != null) {
      guestAuthDTO.phoneNumber(guestAuthRequest.getPhoneNumber());
    }
    if (guestAuthRequest.getEmail() != null) {
      guestAuthDTO.email(guestAuthRequest.getEmail());
    }

    return guestAuthDTO.build();
  }
}
