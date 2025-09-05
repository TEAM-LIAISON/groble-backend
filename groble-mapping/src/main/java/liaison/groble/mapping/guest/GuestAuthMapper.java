package liaison.groble.mapping.guest;

import org.mapstruct.Mapper;

import liaison.groble.api.model.guest.request.GuestAuthCodeRequest;
import liaison.groble.api.model.guest.request.UpdateGuestUserInfoRequest;
import liaison.groble.api.model.guest.request.VerifyGuestAuthCodeRequest;
import liaison.groble.api.model.guest.response.GuestAuthCodeResponse;
import liaison.groble.api.model.guest.response.UpdateGuestUserInfoResponse;
import liaison.groble.api.model.guest.response.VerifyAuthCodeResponse;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.dto.GuestTokenDTO;
import liaison.groble.application.guest.dto.UpdateGuestUserInfoDTO;
import liaison.groble.application.guest.dto.UpdateGuestUserInfoResultDTO;
import liaison.groble.application.guest.dto.VerifyGuestAuthCodeDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface GuestAuthMapper {

  GuestAuthCodeResponse toGuestAuthCodeResponse(GuestAuthDTO guestAuthDTO);

  GuestAuthDTO toGuestAuthDTO(GuestAuthCodeRequest guestAuthCodeRequest);

  VerifyGuestAuthCodeDTO toVerifyGuestAuthCodeDTO(
      VerifyGuestAuthCodeRequest verifyGuestAuthCodeRequest);

  VerifyAuthCodeResponse toVerifyAuthCodeResponse(GuestTokenDTO guestTokenDTO);

  UpdateGuestUserInfoDTO toUpdateGuestUserInfoDTO(
      UpdateGuestUserInfoRequest updateGuestUserInfoRequest);

  UpdateGuestUserInfoResponse toUpdateGuestUserInfoResponse(UpdateGuestUserInfoResultDTO resultDTO);
}
