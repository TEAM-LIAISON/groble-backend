package liaison.groble.api.model.content.response.swagger;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContentsCategoryApiResponse")
public class ContentsCategoryApiResponse
    extends GrobleResponse<PageResponse<ContentPreviewCardResponse>> {}
