package liaison.groble.api.model.purchase.swagger;

import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 콘텐츠 구매 관리 부분에서 전체 목록 응답 모델")
public class PurchasedContentsListResponse
    extends GrobleResponse<PageResponse<PurchaserContentPreviewCardResponse>> {}
