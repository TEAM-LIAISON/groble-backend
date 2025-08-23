package liaison.groble.domain.dashboard.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.dashboard.dto.FlatReferrerStatsDTO;

public interface ContentReferrerStatsCustomRepository {
  Page<FlatReferrerStatsDTO> findContentReferrerStats(
      Long contentId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
