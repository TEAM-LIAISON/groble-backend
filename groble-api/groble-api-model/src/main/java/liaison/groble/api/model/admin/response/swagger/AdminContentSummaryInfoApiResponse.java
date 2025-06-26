package liaison.groble.api.model.admin.response.swagger;

import liaison.groble.api.model.admin.response.AdminContentSummaryInfoResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminContentSummaryInfoApiResponse")
public class AdminContentSummaryInfoApiResponse
    extends GrobleResponse<PageResponse<AdminContentSummaryInfoResponse>> {}
