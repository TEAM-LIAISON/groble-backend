package liaison.groble.api.model.purchase.swagger;

import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MyPurchasingContentsApiResponse")
public class MyPurchasingContentsApiResponse
    extends GrobleResponse<CursorResponse<PurchaserContentPreviewCardResponse>> {}
