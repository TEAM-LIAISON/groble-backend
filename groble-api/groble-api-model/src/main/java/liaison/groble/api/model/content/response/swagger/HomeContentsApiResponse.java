package liaison.groble.api.model.content.response.swagger;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "HomeContentsApiResponse")
public class HomeContentsApiResponse
    extends GrobleResponse<CursorResponse<ContentPreviewCardResponse>> {}
