package liaison.groble.mapping.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.user.request.SetSocialBasicInfoRequest;
import liaison.groble.application.user.dto.SocialBasicInfoDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface UserMapper {

  // ====== 📥 Request → DTO 변환 ======
  @Mapping(
      target = "termsTypeStrings",
      expression = "java(request.getTermsTypes().stream().map(Enum::name).toList())")
  SocialBasicInfoDTO toSocialBasicInfoDTO(SetSocialBasicInfoRequest request);
}
