package liaison.groble.api.model.settlement.response.swagger;

import liaison.groble.api.model.settlement.response.PerTransactionSettlementOverviewResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "총 판매 내역 목록 조회 응답 모델")
public class PerTransactionSettlementOverviewListResponse
    extends GrobleResponse<PageResponse<PerTransactionSettlementOverviewResponse>> {}
