package liaison.groble.application.content.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.entity.ContentViewLog;
import liaison.groble.domain.content.entity.ContentViewStats;
import liaison.groble.domain.content.repository.ContentViewLogRepository;
import liaison.groble.domain.content.repository.ContentViewStatsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ContentStatsAggregationService {
  private final ContentViewLogRepository contentViewLogRepository;
  private final ContentViewStatsRepository contentViewStatsRepository;

  @Transactional
  public void aggregateDailyStats() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    log.info("Daily stats aggregation for {}", yesterday);

    // 기존 집계 삭제 → 없으면 무시
    contentViewStatsRepository.deleteByStatDateAndPeriodType(
        yesterday, ContentViewStats.PeriodType.DAILY);

    // 로그에서 새 집계 수집
    List<ContentViewStats> stats = aggregateFromLogs(yesterday);

    // 새 집계 저장
    contentViewStatsRepository.saveAll(stats);
  }

  public void aggregateMonthlyStats() {
    YearMonth lastMonth = YearMonth.now().minusMonths(1);
    LocalDate monthStart = lastMonth.atDay(1);
    LocalDate monthEnd = lastMonth.atEndOfMonth();
    log.info("Monthly stats aggregation for {} ~ {}", monthStart, monthEnd);

    // 1) 기존 월별 집계 삭제
    contentViewStatsRepository.deleteByStatDateAndPeriodType(
        monthStart, ContentViewStats.PeriodType.MONTHLY);

    // 2) 로그에서 새 집계 수집
    List<ContentViewStats> stats = aggregateMonthlyFromLogs(monthStart, monthEnd);

    // 3) 새 집계 저장
    contentViewStatsRepository.saveAll(stats);
  }

  private List<ContentViewStats> aggregateFromLogs(LocalDate date) {
    LocalDateTime start = date.atStartOfDay();
    LocalDateTime end = start.plusDays(1);

    // 1) 하루치 로그 조회
    List<ContentViewLog> logs = contentViewLogRepository.findByViewedAtBetween(start, end);

    // 2) 마켓별로 그룹핑
    Map<Long, List<ContentViewLog>> grouped =
        logs.stream().collect(Collectors.groupingBy(ContentViewLog::getContentId));

    // 3) 그룹별 집계 및 DTO 변환
    return grouped.entrySet().stream()
        .map(
            entry -> {
              Long contentId = entry.getKey();
              List<ContentViewLog> items = entry.getValue();

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
                      .map(ContentViewLog::getViewerId)
                      .filter(Objects::nonNull)
                      .distinct()
                      .count();

              return ContentViewStats.builder()
                  .contentId(contentId)
                  .statDate(date)
                  .periodType(ContentViewStats.PeriodType.DAILY)
                  .viewCount(viewCount)
                  .uniqueViewerCount(uniqueViewerCount)
                  .loggedInViewerCount(loggedInViewerCount)
                  .build();
            })
        .collect(Collectors.toList());
  }

  private List<ContentViewStats> aggregateMonthlyFromLogs(
      LocalDate monthStart, LocalDate monthEnd) {
    LocalDateTime start = monthStart.atStartOfDay();
    // monthEnd 끝자락까지 포함하려면 다음날 00:00 전까지
    LocalDateTime end = monthEnd.plusDays(1).atStartOfDay();

    // 1) 해당 기간 로그 조회
    List<ContentViewLog> logs = contentViewLogRepository.findByViewedAtBetween(start, end);

    // 2) 마켓별로 그룹핑
    Map<Long, List<ContentViewLog>> grouped =
        logs.stream().collect(Collectors.groupingBy(ContentViewLog::getContentId));

    // 3) 그룹별로 집계 후 DTO 변환
    return grouped.entrySet().stream()
        .map(
            entry -> {
              Long contentId = entry.getKey();
              List<ContentViewLog> items = entry.getValue();

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
                      .map(ContentViewLog::getViewerId)
                      .filter(Objects::nonNull)
                      .distinct()
                      .count();

              return ContentViewStats.builder()
                  .contentId(contentId)
                  .statDate(monthStart) // 월별 통계는 ‘그 달의 첫날’ 기준으로 저장
                  .periodType(ContentViewStats.PeriodType.MONTHLY)
                  .viewCount(viewCount)
                  .uniqueViewerCount(uniqueViewerCount)
                  .loggedInViewerCount(loggedInViewerCount)
                  .build();
            })
        .collect(Collectors.toList());
  }
}
