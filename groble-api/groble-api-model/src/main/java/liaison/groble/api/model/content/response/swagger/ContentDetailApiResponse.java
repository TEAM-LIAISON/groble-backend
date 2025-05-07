package liaison.groble.api.model.content.response.swagger;

import liaison.groble.api.model.content.response.ContentDetailResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContentDetailApiResponse")
public class ContentDetailApiResponse extends GrobleResponse<ContentDetailResponse> {}
