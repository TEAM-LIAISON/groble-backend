package liaison.groble.api.model.admin.response.swagger;

import liaison.groble.api.model.admin.response.AdminOrderSummaryInfoResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminOrderSummaryInfoApiResponse")
public class AdminOrderSummaryInfoApiResponse
    extends GrobleResponse<PageResponse<AdminOrderSummaryInfoResponse>> {}
