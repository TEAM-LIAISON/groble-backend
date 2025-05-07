package liaison.groble.api.model.auth.response.swagger;

import liaison.groble.api.model.auth.response.SignUpResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SignUpApiResponse")
public class SignUpApiResponse extends GrobleResponse<SignUpResponse> {}
