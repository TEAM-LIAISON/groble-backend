package liaison.groble.application.market.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import liaison.groble.application.market.dto.MarketViewCountDTO;
import liaison.groble.domain.market.entity.MarketViewLog;
import liaison.groble.domain.market.repository.MarketViewLogRepository;
import liaison.groble.domain.port.DailyViewPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketViewCountService {

  private final MarketViewLogRepository marketViewLogRepository;

  // Port
  private final DailyViewPort dailyViewPort;

  @Async
  public void recordMarketView(Long marketId, MarketViewCountDTO marketViewCountDTO) {

    // # 일별 조회수
    // view:count:market:123:20250128 → "42"

    // # 중복 방지 (1시간)
    // viewed:market:123:user:456 → "1"
    // viewed:market:123:ip:192.168.1.1:382910 → "1"

    String viewerKey =
        marketViewCountDTO.getUserId() != null
            ? "user:" + marketViewCountDTO.getUserId()
            : "ip:"
                + marketViewCountDTO.getIp()
                + ":"
                + marketViewCountDTO.getUserAgent().hashCode();

    if (dailyViewPort.incrementViewIfNotDuplicate("market", marketId, viewerKey)) {
      // 로그 저장
      MarketViewLog log =
          MarketViewLog.builder()
              .marketId(marketId)
              .viewerId(marketViewCountDTO.getUserId())
              .viewerIp(marketViewCountDTO.getIp())
              .userAgent(marketViewCountDTO.getUserAgent())
              .referer(marketViewCountDTO.getReferer())
              .viewedAt(LocalDateTime.now())
              .build();

      marketViewLogRepository.save(log);
    }
  }
}
