package liaison.groble.api.server.sell;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.application.market.MarketService;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
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

  // 응답 메시지 상수화
  private static final String MARKET_VIEW_INTRO_SUCCESS_MESSAGE =
      "마켓 뷰어 화면에서 메이커 정보 및 대표 콘텐츠 조회에 성공했습니다.";

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
}
