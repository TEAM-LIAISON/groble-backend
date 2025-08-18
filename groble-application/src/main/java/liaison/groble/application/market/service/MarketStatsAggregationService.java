package liaison.groble.application.market.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.dashboard.entity.MarketViewLog;
import liaison.groble.domain.dashboard.entity.MarketViewStats;
import liaison.groble.domain.dashboard.repository.MarketViewLogRepository;
import liaison.groble.domain.dashboard.repository.MarketViewStatsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MarketStatsAggregationService {
  private final MarketViewLogRepository marketViewLogRepository;
  private final MarketViewStatsRepository marketViewStatsRepository;

  @Transactional
  public void aggregateDailyStats() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    log.info("Daily stats aggregation for {}", yesterday);

    // 기존 집계 삭제 → 없으면 무시
    marketViewStatsRepository.deleteByStatDateAndPeriodType(yesterday, PeriodType.DAILY);

    // 로그에서 새 집계 수집
    List<MarketViewStats> stats = aggregateFromLogs(yesterday);

    // 새 집계 저장
    marketViewStatsRepository.saveAll(stats);
  }

  public void aggregateMonthlyStats() {
    YearMonth lastMonth = YearMonth.now().minusMonths(1);
    LocalDate monthStart = lastMonth.atDay(1);
    LocalDate monthEnd = lastMonth.atEndOfMonth();
    log.info("Monthly stats aggregation for {} ~ {}", monthStart, monthEnd);

    // 1) 기존 월별 집계 삭제
    marketViewStatsRepository.deleteByStatDateAndPeriodType(monthStart, PeriodType.MONTHLY);

    // 2) 로그에서 새 집계 수집
    List<MarketViewStats> stats = aggregateMonthlyFromLogs(monthStart, monthEnd);

    // 3) 새 집계 저장
    marketViewStatsRepository.saveAll(stats);
  }

  private List<MarketViewStats> aggregateFromLogs(LocalDate date) {
    LocalDateTime start = date.atStartOfDay();
    LocalDateTime end = start.plusDays(1);

    // 1) 하루치 로그 조회
    List<MarketViewLog> logs = marketViewLogRepository.findByViewedAtBetween(start, end);

    // 2) 마켓별로 그룹핑
    Map<Long, List<MarketViewLog>> grouped =
        logs.stream().collect(Collectors.groupingBy(MarketViewLog::getMarketId));

    // 3) 그룹별 집계 및 DTO 변환
    return grouped.entrySet().stream()
        .map(
            entry -> {
              Long marketId = entry.getKey();
              List<MarketViewLog> items = entry.getValue();

              long viewCount = items.size();

              long uniqueViewerCount =
                  items.stream()
                      .map(
                          log -> {
                            // 로그인 유저가 있으면 ID, 아니면 IP로 식별
                            return log.getViewerId() != null
                                ? log.getViewerId().toString()
                                : log.getViewerIp();
                          })
                      .filter(Objects::nonNull)
                      .distinct()
                      .count();

              long loggedInViewerCount =
                  items.stream()
                      .map(MarketViewLog::getViewerId)
                      .filter(Objects::nonNull)
                      .distinct()
                      .count();

              return MarketViewStats.builder()
                  .marketId(marketId)
                  .statDate(date)
                  .periodType(PeriodType.DAILY)
                  .viewCount(viewCount)
                  .uniqueViewerCount(uniqueViewerCount)
                  .loggedInViewerCount(loggedInViewerCount)
                  .build();
            })
        .collect(Collectors.toList());
  }

  private List<MarketViewStats> aggregateMonthlyFromLogs(LocalDate monthStart, LocalDate monthEnd) {
    LocalDateTime start = monthStart.atStartOfDay();
    // monthEnd 끝자락까지 포함하려면 다음날 00:00 전까지
    LocalDateTime end = monthEnd.plusDays(1).atStartOfDay();

    // 1) 해당 기간 로그 조회
    List<MarketViewLog> logs = marketViewLogRepository.findByViewedAtBetween(start, end);

    // 2) 마켓별로 그룹핑
    Map<Long, List<MarketViewLog>> grouped =
        logs.stream().collect(Collectors.groupingBy(MarketViewLog::getMarketId));

    // 3) 그룹별로 집계 후 DTO 변환
    return grouped.entrySet().stream()
        .map(
            entry -> {
              Long marketId = entry.getKey();
              List<MarketViewLog> items = entry.getValue();

              long viewCount = items.size();

              long uniqueViewerCount =
                  items.stream()
                      .map(
                          log ->
                              log.getViewerId() != null
                                  ? log.getViewerId().toString()
                                  : log.getViewerIp())
                      .filter(Objects::nonNull)
                      .distinct()
                      .count();

              long loggedInViewerCount =
                  items.stream()
                      .map(MarketViewLog::getViewerId)
                      .filter(Objects::nonNull)
                      .distinct()
                      .count();

              return MarketViewStats.builder()
                  .marketId(marketId)
                  .statDate(monthStart) // 월별 통계는 ‘그 달의 첫날’ 기준으로 저장
                  .periodType(PeriodType.MONTHLY)
                  .viewCount(viewCount)
                  .uniqueViewerCount(uniqueViewerCount)
                  .loggedInViewerCount(loggedInViewerCount)
                  .build();
            })
        .collect(Collectors.toList());
  }
}
