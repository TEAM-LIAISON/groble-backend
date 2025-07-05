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
import liaison.groble.api.model.content.response.swagger.ContentListResponse;
import liaison.groble.api.model.maker.request.MarketEditRequest;
import liaison.groble.api.model.maker.request.MarketLinkCheckRequest;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.market.MarketService;
import liaison.groble.application.market.dto.MarketEditDTO;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.application.market.dto.MarketLinkCheckDTO;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.content.ContentMapper;
import liaison.groble.mapping.market.MarketMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
  private static final String MARKET_EDIT_INTRO_PATH = "/edit/intro";
  private static final String MARKET_INTRO_PATH = "/intro/{marketName}";
  private static final String MARKET_CONTENTS_PATH = "/contents/{marketName}";
  private static final String MARKET_EDIT_PATH = "/edit";
  private static final String MARKET_LINK_CHECK_PATH = "/link-check";

  // 응답 메시지 상수화
  private static final String MARKET_EDIT_INTRO_SUCCESS_MESSAGE =
      "마켓 수정창 화면에서 메이커 정보 및 대표 콘텐츠 조회에 성공했습니다.";
  private static final String MARKET_INTRO_SUCCESS_MESSAGE =
      "마켓 뷰어 화면에서 메이커 정보 및 대표 콘텐츠 조회에 성공했습니다.";
  private static final String MARKET_CONTENTS_SUCCESS_MESSAGE = "마켓 뷰어 화면에서 콘텐츠 목록 전체 조회에 성공했습니다.";
  private static final String MARKET_EDIT_SUCCESS_MESSAGE = "마켓 관리 수정창에서 수정 완료 항목을 저장했습니다.";
  private static final String MARKET_LINK_CHECK_SUCCESS_MESSAGE = "사용 가능한 마켓 링크입니다.";

  private final ContentMapper contentMapper;
  private final MarketMapper marketMapper;
  private final MarketService marketService;
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[✅ 마켓 관리] 마켓 수정창 화면에서 메이커 정보 및 대표 콘텐츠 조회",
      description = "마켓 수정창 화면에서 메이커 정보를 조회하고 대표 콘텐츠가 존재한다면, 대표 콘텐츠 1개에 대한 정보를 반환합니다.")
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = MakerIntroSectionResponse.class)))
  @GetMapping(MARKET_EDIT_INTRO_PATH)
  public ResponseEntity<GrobleResponse<MakerIntroSectionResponse>> getEditIntroSection(
      @Auth Accessor accessor) {
    MarketIntroSectionDTO dto = marketService.getEditIntroSection(accessor.getUserId());
    MakerIntroSectionResponse response = marketMapper.toMakerIntroSectionResponse(dto);

    return responseHelper.success(response, MARKET_EDIT_INTRO_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 마켓 관리] 마켓 뷰어 화면에서 메이커 정보 및 대표 콘텐츠 조회",
      description = "마켓 뷰어 화면에서 메이커 정보를 조회하고 대표 콘텐츠가 존재한다면, 대표 콘텐츠 1개에 대한 정보를 반환합니다.")
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = MakerIntroSectionResponse.class)))
  @GetMapping(MARKET_INTRO_PATH)
  public ResponseEntity<GrobleResponse<MakerIntroSectionResponse>> getViewerMakerIntroSection(
      @Valid @PathVariable("marketName") String marketName) {
    MarketIntroSectionDTO dto = marketService.getViewerMakerIntroSection(marketName);
    MakerIntroSectionResponse response = marketMapper.toMakerIntroSectionResponse(dto);

    return responseHelper.success(response, MARKET_INTRO_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 마켓 관리] 마켓 뷰어 화면에서 콘텐츠 목록 전체 조회",
      description = "마켓 뷰어 화면에서 모든 판매중인 콘텐츠 목록을 조회합니다. 대표 콘텐츠는 포함되지 않습니다.")
  @ApiResponse(
      responseCode = "200",
      description = "마켓 뷰어 화면에서 콘텐츠 목록 전체 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ContentListResponse.class)))
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
        contentMapper.toContentPreviewCardResponsePage(dtoPageResponse);

    return responseHelper.success(responsePage, MARKET_CONTENTS_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 마켓 관리] 마켓 관리 수정창에서 수정 완료 항목을 저장",
      description = "마켓 관리 수정창에서 수정 완료한 항목을 저장합니다.")
  @PostMapping(MARKET_EDIT_PATH)
  public ResponseEntity<GrobleResponse<Void>> editMarket(
      @Auth Accessor accessor,
      @Parameter(description = "마켓 수정 정보", required = true) @Valid @RequestBody
          MarketEditRequest marketEditRequest) {

    MarketEditDTO marketEditDTO = marketMapper.toMarketEditDTO(marketEditRequest);
    marketService.editMarket(accessor.getUserId(), marketEditDTO);

    return responseHelper.success(null, MARKET_EDIT_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(
      summary = "[✅ 마켓 관리] 사용 가능한 마켓 링크 확인",
      description = "마켓 관리에서 사용 가능한 마켓 링크를 확인합니다. 이미 사용 중인 링크는 사용할 수 없습니다.")
  @GetMapping(MARKET_LINK_CHECK_PATH)
  public ResponseEntity<GrobleResponse<Void>> checkMarketLink(
      @Parameter(description = "마켓 수정 정보", required = true) @Valid @RequestBody
          MarketLinkCheckRequest marketLinkCheckRequest) {

    MarketLinkCheckDTO marketLinkCheckDTO =
        marketMapper.toMarketLinkCheckDTO(marketLinkCheckRequest);
    marketService.checkMarketLink(marketLinkCheckDTO);

    return responseHelper.success(null, MARKET_LINK_CHECK_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
