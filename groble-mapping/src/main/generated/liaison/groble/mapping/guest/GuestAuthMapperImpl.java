package liaison.groble.mapping.guest;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.guest.request.GuestAuthCodeRequest;
import liaison.groble.api.model.guest.request.GuestAuthVerifyRequest;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.dto.GuestAuthVerifyDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-29T19:52:03+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class GuestAuthMapperImpl implements GuestAuthMapper {

  @Override
  public GuestAuthDTO toGuestAuthDTO(GuestAuthCodeRequest guestAuthCodeRequest) {
    if (guestAuthCodeRequest == null) {
      return null;
    }

    GuestAuthDTO.GuestAuthDTOBuilder guestAuthDTO = GuestAuthDTO.builder();

    if (guestAuthCodeRequest.getUsername() != null) {
      guestAuthDTO.username(guestAuthCodeRequest.getUsername());
    }
    if (guestAuthCodeRequest.getEmail() != null) {
      guestAuthDTO.email(guestAuthCodeRequest.getEmail());
    }
    if (guestAuthCodeRequest.getPhoneNumber() != null) {
      guestAuthDTO.phoneNumber(guestAuthCodeRequest.getPhoneNumber());
    }

    return guestAuthDTO.build();
  }

  @Override
  public GuestAuthVerifyDTO toGuestAuthVerifyDTO(GuestAuthVerifyRequest guestAuthVerifyRequest) {
    if (guestAuthVerifyRequest == null) {
      return null;
    }

    GuestAuthVerifyDTO.GuestAuthVerifyDTOBuilder guestAuthVerifyDTO = GuestAuthVerifyDTO.builder();

    if (guestAuthVerifyRequest.getUsername() != null) {
      guestAuthVerifyDTO.username(guestAuthVerifyRequest.getUsername());
    }
    if (guestAuthVerifyRequest.getEmail() != null) {
      guestAuthVerifyDTO.email(guestAuthVerifyRequest.getEmail());
    }
    if (guestAuthVerifyRequest.getPhoneNumber() != null) {
      guestAuthVerifyDTO.phoneNumber(guestAuthVerifyRequest.getPhoneNumber());
    }
    if (guestAuthVerifyRequest.getAuthCode() != null) {
      guestAuthVerifyDTO.authCode(guestAuthVerifyRequest.getAuthCode());
    }

    return guestAuthVerifyDTO.build();
  }
}
