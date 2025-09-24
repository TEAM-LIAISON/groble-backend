package liaison.groble.mapping.hometest;

import org.mapstruct.Mapper;

import liaison.groble.api.model.hometest.phoneauth.request.HomeTestPhoneAuthCodeRequest;
import liaison.groble.api.model.hometest.phoneauth.request.HomeTestVerifyPhoneAuthRequest;
import liaison.groble.api.model.hometest.phoneauth.response.HomeTestPhoneAuthCodeResponse;
import liaison.groble.api.model.hometest.phoneauth.response.HomeTestVerifyPhoneAuthResponse;
import liaison.groble.application.hometest.dto.HomeTestPhoneAuthDTO;
import liaison.groble.application.hometest.dto.HomeTestVerificationResultDTO;
import liaison.groble.application.hometest.dto.HomeTestVerifyAuthDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface HomeTestAuthMapper {

  HomeTestPhoneAuthDTO toPhoneAuthDTO(HomeTestPhoneAuthCodeRequest request);

  HomeTestVerifyAuthDTO toVerifyAuthDTO(HomeTestVerifyPhoneAuthRequest request);

  HomeTestPhoneAuthCodeResponse toPhoneAuthResponse(HomeTestPhoneAuthDTO dto);

  HomeTestVerifyPhoneAuthResponse toVerifyResponse(HomeTestVerificationResultDTO dto);
}
