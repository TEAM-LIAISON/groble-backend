package liaison.groble.api.model.admin.response.swagger;

import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminUserSummaryInfoApiResponse")
public class AdminUserSummaryInfoApiResponse
    extends GrobleResponse<PageResponse<AdminUserSummaryInfoResponse>> {}
