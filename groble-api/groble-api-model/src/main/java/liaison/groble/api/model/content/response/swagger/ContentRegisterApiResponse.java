package liaison.groble.api.model.content.response.swagger;

import liaison.groble.api.model.content.response.ContentResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContentRegisterApiResponse")
public class ContentRegisterApiResponse extends GrobleResponse<ContentResponse> {}
