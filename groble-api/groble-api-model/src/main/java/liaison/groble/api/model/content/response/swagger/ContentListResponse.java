package liaison.groble.api.model.content.response.swagger;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘텐츠 목록 응답")
public class ContentListResponse extends GrobleResponse<PageResponse<ContentPreviewCardResponse>> {}
