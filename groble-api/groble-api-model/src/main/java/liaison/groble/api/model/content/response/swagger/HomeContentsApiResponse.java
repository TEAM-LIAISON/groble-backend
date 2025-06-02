package liaison.groble.api.model.content.response.swagger;

import liaison.groble.api.model.content.response.HomeContentsResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "HomeContentsApiResponse")
public class HomeContentsApiResponse extends GrobleResponse<HomeContentsResponse> {}
