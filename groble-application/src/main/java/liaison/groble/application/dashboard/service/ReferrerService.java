package liaison.groble.application.dashboard.service;

import org.springframework.stereotype.Service;

import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.dashboard.entity.ContentReferrerStats;
import liaison.groble.domain.dashboard.entity.MarketReferrerStats;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsRepository;
import liaison.groble.domain.dashboard.repository.MarketReferrerStatsRepository;
import liaison.groble.domain.market.entity.Market;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReferrerService {
  private final ContentReferrerStatsRepository contentReferrerStatsRepository;
  private final MarketReferrerStatsRepository marketReferrerStatsRepository;
  private final UserReader userReader;

  public void recordContentReferrer(Long contentId, ReferrerDTO referrerDTO) {
    ContentReferrerStats stats =
        ContentReferrerStats.builder()
            .contentId(contentId)
            .referrerUrl(referrerDTO.getReferrerUrl())
            .source(referrerDTO.getUtmSource())
            .medium(referrerDTO.getUtmMedium())
            .campaign(referrerDTO.getUtmCampaign())
            .content(referrerDTO.getUtmContent())
            .term(referrerDTO.getUtmTerm())
            .build();

    // referrerUrl 파싱하여 도메인과 경로 추출
    stats.parseReferrerUrl();

    contentReferrerStatsRepository.save(stats);
  }

  public void recordMarketReferrer(String marketLinkUrl, ReferrerDTO referrerDTO) {
    Market market = userReader.getMarketWithUser(marketLinkUrl);

    MarketReferrerStats stats =
        MarketReferrerStats.builder()
            .marketId(market.getId())
            .referrerUrl(referrerDTO.getReferrerUrl())
            .source(referrerDTO.getUtmSource())
            .medium(referrerDTO.getUtmMedium())
            .campaign(referrerDTO.getUtmCampaign())
            .content(referrerDTO.getUtmContent())
            .term(referrerDTO.getUtmTerm())
            .build();
    // referrerUrl 파싱하여 도메인과 경로 추출
    stats.parseReferrerUrl();

    marketReferrerStatsRepository.save(stats);
  }
}
