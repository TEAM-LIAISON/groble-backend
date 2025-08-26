package liaison.groble.api.model.dashboard.response.swagger;

import liaison.groble.api.model.dashboard.response.ReferrerStatsResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유입 경로 목록 조회 응답 모델")
public class ReferrerStatsListResponse
    extends GrobleResponse<PageResponse<ReferrerStatsResponse>> {}
