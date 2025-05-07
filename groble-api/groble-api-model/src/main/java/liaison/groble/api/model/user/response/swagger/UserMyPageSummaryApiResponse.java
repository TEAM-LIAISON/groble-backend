package liaison.groble.api.model.user.response.swagger;

import liaison.groble.api.model.user.response.UserMyPageSummaryResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserMyPageSummaryApiResponse")
public class UserMyPageSummaryApiResponse extends GrobleResponse<UserMyPageSummaryResponse> {}
