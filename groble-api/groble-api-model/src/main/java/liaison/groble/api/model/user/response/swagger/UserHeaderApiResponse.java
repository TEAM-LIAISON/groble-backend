package liaison.groble.api.model.user.response.swagger;

import liaison.groble.api.model.user.response.UserHeaderResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserHeaderApiResponse")
public class UserHeaderApiResponse extends GrobleResponse<UserHeaderResponse> {}
