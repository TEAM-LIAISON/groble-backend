package liaison.groble.api.server.user.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.user.enums.UserTypeDto;
import liaison.groble.api.model.user.response.UserMyPageResponse;
import liaison.groble.application.user.dto.UserMyPageDto;

@Component
public class UserDtoMapper {

  public UserMyPageResponse toApiMyPageResponse(UserMyPageDto userMyPageDto) {

    UserTypeDto userTypeDto =
        userMyPageDto.getUserTypeDescription() != null
            ? UserTypeDto.valueOf(userMyPageDto.getUserTypeDescription())
            : null;

    return UserMyPageResponse.builder()
        .nickName(userMyPageDto.getNickName())
        .profileImageUrl(userMyPageDto.getProfileImageUrl())
        .userType(userTypeDto)
        .build();
  }
}
