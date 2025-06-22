package liaison.groble.api.server.sell;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/market")
public class MarketController {
  // API 경로 상수화
  private static final String MARKET_VIEW_PATH = "/view/{marketName}";

  //    @GetMapping(MARKET_VIEW_PATH)
  //    public ResponseEntity<GrobleResponse<>>
}
