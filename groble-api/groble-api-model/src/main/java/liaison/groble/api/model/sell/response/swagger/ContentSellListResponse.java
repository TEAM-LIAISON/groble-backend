package liaison.groble.api.model.sell.response.swagger;

import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "특정 콘텐츠의 판매 콘텐츠 전체 목록 응답 모델")
public class ContentSellListResponse
    extends GrobleResponse<PageResponse<ContentSellDetailResponse>> {}
