package liaison.groble.mapping.user;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.user.request.SetSocialBasicInfoRequest;
import liaison.groble.application.user.dto.SocialBasicInfoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T21:45:28+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class UserMapperImpl implements UserMapper {

  @Override
  public SocialBasicInfoDTO toSocialBasicInfoDTO(SetSocialBasicInfoRequest request) {
    if (request == null) {
      return null;
    }

    SocialBasicInfoDTO.SocialBasicInfoDTOBuilder socialBasicInfoDTO = SocialBasicInfoDTO.builder();

    if (request.getUserType() != null) {
      socialBasicInfoDTO.userType(request.getUserType());
    }

    socialBasicInfoDTO.termsTypeStrings(request.getTermsTypes().stream().map(Enum::name).toList());

    return socialBasicInfoDTO.build();
  }
}
