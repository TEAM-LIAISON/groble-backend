package liaison.groble.api.model.dashboard.response.swagger;

import liaison.groble.api.model.dashboard.response.MarketViewStatsResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마켓 날짜별 조회수 목록 조회 응답 모델")
public class MarketViewStatsListResponse
    extends GrobleResponse<PageResponse<MarketViewStatsResponse>> {}
