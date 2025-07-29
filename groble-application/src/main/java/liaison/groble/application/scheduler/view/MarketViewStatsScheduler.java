package liaison.groble.application.scheduler.view;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.market.service.MarketStatsAggregationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketViewStatsScheduler {

  private final MarketStatsAggregationService aggregationService;

  @Scheduled(cron = "0 01 0 * * ?") // 매일 00:00 실행
  @Transactional
  public void aggregateDailyStats() {
    try {
      aggregationService.aggregateDailyStats();
    } catch (Exception e) {
      log.error("일별 마켓 조회수 집계 실패", e);
    }
  }

  @Scheduled(cron = "0 05 0 1 * ?") // 매월 1일 00:05 실행
  @Transactional
  public void aggregateMonthlyStats() {
    try {
      aggregationService.aggregateMonthlyStats();
    } catch (Exception e) {
      log.error("월별 조회수 집계 실패", e);
    }
  }
}
