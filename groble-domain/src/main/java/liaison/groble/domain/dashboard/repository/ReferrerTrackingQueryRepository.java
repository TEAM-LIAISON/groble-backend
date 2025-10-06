package liaison.groble.domain.dashboard.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.dashboard.dto.FlatReferrerStatsDTO;

public interface ReferrerTrackingQueryRepository {

  Page<FlatReferrerStatsDTO> findContentReferrerStats(
      Long contentId, LocalDate startDate, LocalDate endDate, Pageable pageable);

  Page<FlatReferrerStatsDTO> findMarketReferrerStats(
      String marketLinkUrl, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
