package liaison.groble.api.model.scrap.response.swagger;

import liaison.groble.api.model.content.response.ContentScrapCardResponse;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ContentScrapCardApiResponse",
    description = "스크랩한 콘텐츠 목록 조회 응답",
    title = "스크랩 콘텐츠 응답")
public class ContentScrapCardApiResponse
    extends GrobleResponse<CursorResponse<ContentScrapCardResponse>> {
  // 표준 응답 패턴을 따르므로 추가 필드 불필요
}
