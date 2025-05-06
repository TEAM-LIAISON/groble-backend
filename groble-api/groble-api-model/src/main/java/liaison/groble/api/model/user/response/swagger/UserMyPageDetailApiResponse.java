package liaison.groble.api.model.user.response.swagger;

import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserMyPageDetailApiResponse")
public class UserMyPageDetailApiResponse extends GrobleResponse<UserMyPageDetailResponse> {}
