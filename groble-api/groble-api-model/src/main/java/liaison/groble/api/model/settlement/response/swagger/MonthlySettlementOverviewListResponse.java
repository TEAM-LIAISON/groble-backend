package liaison.groble.api.model.settlement.response.swagger;

import liaison.groble.api.model.settlement.response.SettlementsOverviewResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정산 내역 전체 목록 조회 응답 모델")
public class MonthlySettlementOverviewListResponse
    extends GrobleResponse<PageResponse<SettlementsOverviewResponse>> {}
