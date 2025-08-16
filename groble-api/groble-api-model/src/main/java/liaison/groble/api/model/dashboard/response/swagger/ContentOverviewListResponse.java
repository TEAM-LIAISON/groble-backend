package liaison.groble.api.model.dashboard.response.swagger;

import liaison.groble.api.model.dashboard.response.ContentOverviewResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드 내 콘텐츠 목록 조회 응답 모델")
public class ContentOverviewListResponse
    extends GrobleResponse<PageResponse<ContentOverviewResponse>> {}
