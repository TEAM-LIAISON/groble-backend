package liaison.groble.api.model.sell.response.swagger;

import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "특정 콘텐츠에 남겨진 콘텐츠 리뷰 목록 응답 모델")
public class ContentReviewListResponse
    extends GrobleResponse<PageResponse<ContentReviewDetailResponse>> {}
