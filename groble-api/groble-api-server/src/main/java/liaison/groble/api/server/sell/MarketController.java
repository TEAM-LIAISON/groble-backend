package liaison.groble.api.server.sell;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.market.MarketService;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.market.MarketMapper;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/market")
public class MarketController {

  // API 경로 상수화
  private static final String MARKET_VIEW_INTRO_PATH = "/view/intro/{marketName}";
  private static final String MARKET_VIEW_CONTENTS_PATH = "/view/contents/{marketName}";

  // 응답 메시지 상수화
  private static final String MARKET_VIEW_INTRO_SUCCESS_MESSAGE =
      "마켓 뷰어 화면에서 메이커 정보 및 대표 콘텐츠 조회에 성공했습니다.";
  private static final String MARKET_VIEW_CONTENTS_SUCCESS_MESSAGE =
      "마켓 뷰어 화면에서 콘텐츠 목록 전체 조회에 성공했습니다.";

  private final MarketMapper marketMapper;
  private final MarketService marketService;
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[🏷 마켓] 마켓 뷰어 화면에서 메이커 정보 및 대표 콘텐츠 조회",
      description = "마켓 뷰어 화면에서 메이커 정보를 조회하고 대표 콘텐츠가 존재한다면, 대표 콘텐츠 1개에 대한 정보를 반환합니다.")
  @GetMapping(MARKET_VIEW_INTRO_PATH)
  public ResponseEntity<GrobleResponse<MakerIntroSectionResponse>> getViewerMakerIntroSection(
      @Valid @PathVariable("marketName") String marketName) {
    MarketIntroSectionDTO dto = marketService.getViewerMakerIntroSection(marketName);
    MakerIntroSectionResponse response = marketMapper.toMakerIntroSectionResponse(dto);

    return responseHelper.success(response, MARKET_VIEW_INTRO_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[🏷 마켓] 마켓 뷰어 화면에서 콘텐츠 목록 전체 조회",
      description = "마켓 뷰어 화면에서 모든 판매중인 콘텐츠 목록을 조회합니다.")
  @GetMapping(MARKET_VIEW_CONTENTS_PATH)
  public ResponseEntity<GrobleResponse<PageResponse<ContentPreviewCardResponse>>> getViewerContents(
      @Valid @PathVariable("marketName") String marketName,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "12") int size,
      @RequestParam(value = "sort", defaultValue = "createdAt") String sort) {

    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<ContentCardDTO> dtoPageResponse =
        marketService.getMarketContents(marketName, pageable);
    PageResponse<ContentPreviewCardResponse> responsePage =
        marketMapper.toContentPreviewCardResponsePage(dtoPageResponse);
    return responseHelper.success(
        responsePage, MARKET_VIEW_CONTENTS_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
