package liaison.groble.application.content.service;

import org.springframework.stereotype.Service;

import liaison.groble.application.content.dto.referrer.ContentReferrerDTO;
import liaison.groble.domain.dashboard.entity.ContentReferrerStats;
import liaison.groble.domain.dashboard.repository.ContentReferrerStatsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentReferrerService {
  private final ContentReferrerStatsRepository contentReferrerStatsRepository;

  public void recordContentReferrer(Long contentId, ContentReferrerDTO contentReferrerDTO) {
    ContentReferrerStats stats =
        ContentReferrerStats.builder()
            .contentId(contentId)
            .referrerUrl(contentReferrerDTO.getReferrerUrl())
            .source(contentReferrerDTO.getUtmSource())
            .medium(contentReferrerDTO.getUtmMedium())
            .campaign(contentReferrerDTO.getUtmCampaign())
            .content(contentReferrerDTO.getUtmContent())
            .term(contentReferrerDTO.getUtmTerm())
            .build();

    // referrerUrl 파싱하여 도메인과 경로 추출
    stats.parseReferrerUrl();

    contentReferrerStatsRepository.save(stats);
  }
}
