package liaison.groble.api.server.market;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.maker.request.MarketEditRequest;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.market.MarketService;
import liaison.groble.application.market.dto.MarketEditDTO;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.market.MarketMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "[🏷 마켓] 마켓 관리 및 마켓 뷰어 API", description = "마켓 관리 및 마켓 뷰어 화면에서 사용되는 모든 기능을 제공합니다.")
@RequestMapping("/api/v1/market")
public class MarketController {

  // API 경로 상수화
  private static final String MARKET_INTRO_PATH = "/intro/{marketName}";
  private static final String MARKET_CONTENTS_PATH = "/contents/{marketName}";
  private static final String MARKET_EDIT_PATH = "/edit/{marketName}";

  // 응답 메시지 상수화
  private static final String MARKET_INTRO_SUCCESS_MESSAGE =
      "마켓 뷰어 화면에서 메이커 정보 및 대표 콘텐츠 조회에 성공했습니다.";
  private static final String MARKET_CONTENTS_SUCCESS_MESSAGE = "마켓 뷰어 화면에서 콘텐츠 목록 전체 조회에 성공했습니다.";
  private static final String MARKET_EDIT_SUCCESS_MESSAGE = "마켓 관리 수정창에서 수정 완료 항목을 저장했습니다.";

  private final MarketMapper marketMapper;
  private final MarketService marketService;
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[❌ 마켓] 마켓 뷰어 화면에서 메이커 정보 및 대표 콘텐츠 조회",
      description = "마켓 뷰어 화면에서 메이커 정보를 조회하고 대표 콘텐츠가 존재한다면, 대표 콘텐츠 1개에 대한 정보를 반환합니다.")
  @GetMapping(MARKET_INTRO_PATH)
  public ResponseEntity<GrobleResponse<MakerIntroSectionResponse>> getViewerMakerIntroSection(
      @Valid @PathVariable("marketName") String marketName) {
    MarketIntroSectionDTO dto = marketService.getViewerMakerIntroSection(marketName);
    MakerIntroSectionResponse response = marketMapper.toMakerIntroSectionResponse(dto);

    return responseHelper.success(response, MARKET_INTRO_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[❌ 마켓] 마켓 뷰어 화면에서 콘텐츠 목록 전체 조회",
      description = "마켓 뷰어 화면에서 모든 판매중인 콘텐츠 목록을 조회합니다.")
  @GetMapping(MARKET_CONTENTS_PATH)
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
    return responseHelper.success(responsePage, MARKET_CONTENTS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  // TODO: 1. 마켓 관리 수정창에서 수정 완료 항목을 저장 (완료 버튼)
  @Operation(
      summary = "[❌ 마켓] 마켓 관리 수정창에서 수정 완료 항목을 저장",
      description = "마켓 관리 수정창에서 수정 완료한 항목을 저장합니다.")
  @PostMapping(MARKET_EDIT_PATH)
  public ResponseEntity<GrobleResponse<Void>> editMarket(
      @Auth Accessor accessor,
      @Valid @PathVariable("marketName") String marketName,
      @Parameter(description = "마켓 수정 정보", required = true) @Valid @RequestBody
          MarketEditRequest marketEditRequest) {

    MarketEditDTO marketEditDTO = marketMapper.toMarketEditDTO(marketEditRequest);

    marketService.editMarket(accessor.getUserId(), marketName, marketEditDTO);

    return responseHelper.success(null, MARKET_EDIT_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
