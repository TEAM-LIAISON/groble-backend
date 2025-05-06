package liaison.groble.api.model.user.response.swagger;

import liaison.groble.api.model.user.response.BuyerMyPageSummaryResponse;
import liaison.groble.api.model.user.response.SellerMyPageSummaryResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "UserMyPageSummaryApiResponse",
    oneOf = {BuyerMyPageSummaryResponse.class, SellerMyPageSummaryResponse.class})
public class UserMyPageSummaryApiResponse extends GrobleResponse<Object> {}
