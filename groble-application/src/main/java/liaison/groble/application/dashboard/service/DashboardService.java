package liaison.groble.application.dashboard.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.dashboard.dto.ContentTotalViewStatsDTO;
import liaison.groble.application.dashboard.dto.ContentViewStatsDTO;
import liaison.groble.application.dashboard.dto.DashboardContentOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;
import liaison.groble.application.dashboard.dto.DashboardViewStatsDTO;
import liaison.groble.application.dashboard.dto.MarketViewStatsDTO;
import liaison.groble.application.dashboard.dto.referrer.ReferrerStatsDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.common.enums.PeriodType;
import liaison.groble.domain.content.dto.FlatContentOverviewDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.dashboard.dto.FlatContentTotalViewStatsDTO;
import liaison.groble.domain.dashboard.dto.FlatContentViewStatsDTO;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.dashboard.dto.FlatMarketViewStatsDTO;
import liaison.groble.domain.dashboard.dto.FlatReferrerStatsDTO;
import liaison.groble.domain.dashboard.repository.ContentViewStatsCustomRepository;
import liaison.groble.domain.dashboard.repository.ContentViewStatsRepository;
import liaison.groble.domain.dashboard.repository.MarketViewStatsCustomRepository;
import liaison.groble.domain.dashboard.repository.MarketViewStatsRepository;
import liaison.groble.domain.dashboard.repository.ReferrerTrackingQueryRepository;
import liaison.groble.domain.dashboard.support.ReferrerDomainUtils;
import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.user.entity.SellerInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
  private static final Set<String> SOCIAL_DOMAIN_KEYWORDS =
      Set.of(
          "instagram.com",
          "threads.com",
          "facebook.com",
          "fb.com",
          "tiktok.com",
          "youtube.com",
          "twitter.com",
          "x.com",
          "linkedin.com",
          "kakao.com",
          "kakaocorp.com",
          "kakao.co.kr",
          "t.co");
  private static final Set<String> SEARCH_DOMAIN_KEYWORDS =
      Set.of("google.", "naver.", "bing.", "daum.", "yahoo.", "baidu.", "duckduckgo.");

  // Reader
  private final UserReader userReader;
  private final ContentReader contentReader;
  private final PurchaseReader purchaseReader;

  // Repository
  private final ContentViewStatsRepository contentViewStatsRepository;
  private final ContentViewStatsCustomRepository contentViewStatsCustomRepository;
  private final ReferrerTrackingQueryRepository referrerTrackingQueryRepository;
  private final MarketViewStatsRepository marketViewStatsRepository;
  private final MarketViewStatsCustomRepository marketViewStatsCustomRepository;

  @Transactional(readOnly = true)
  public DashboardOverviewDTO getDashboardOverview(Long userId) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    FlatDashboardOverviewDTO flatDashboardOverviewDTO =
        purchaseReader.getDashboardOverviewStats(userId);

    Long totalContentViews =
        getTotalContentViews(userId, LocalDate.of(2025, 8, 1), LocalDate.now());
    Long totalMarketViews = getTotalMarketViews(userId, LocalDate.of(2025, 8, 1), LocalDate.now());

    return DashboardOverviewDTO.builder()
        .verificationStatus(sellerInfo.getVerificationStatus().name())
        .totalRevenue(flatDashboardOverviewDTO.getTotalRevenue())
        .totalSalesCount(flatDashboardOverviewDTO.getTotalSalesCount())
        .currentMonthRevenue(flatDashboardOverviewDTO.getCurrentMonthRevenue())
        .currentMonthSalesCount(flatDashboardOverviewDTO.getCurrentMonthSalesCount())
        .totalMarketViews(totalMarketViews)
        .totalContentViews(totalContentViews)
        .totalCustomers(flatDashboardOverviewDTO.getTotalCustomers())
        .recentCustomers(flatDashboardOverviewDTO.getRecentCustomers())
        .build();
  }

  @Transactional(readOnly = true)
  public PageResponse<DashboardContentOverviewDTO> getMyContentsList(
      Long userId, Pageable pageable) {
    Page<FlatContentOverviewDTO> page = contentReader.findMyContentsBySellerId(userId, pageable);

    List<DashboardContentOverviewDTO> items =
        page.getContent().stream().map(this::toDashboardContentOverviewDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public DashboardViewStatsDTO getViewStats(Long userId, String period) {
    LocalDate endDate;
    LocalDate startDate;

    // LAST_MONTH의 경우 endDate도 지난달 마지막 날로 설정
    switch (period) {
      case "TODAY" -> {
        startDate = LocalDate.now();
        endDate = LocalDate.now();
      }
      case "LAST_7_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(6);
      }
      case "LAST_30_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(29);
      }
      case "THIS_MONTH" -> {
        endDate = LocalDate.now();
        startDate = endDate.withDayOfMonth(1);
      }
      case "LAST_MONTH" -> {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        startDate = lastMonth.atDay(1);
        endDate = lastMonth.atEndOfMonth(); // 지난달 마지막 날
      }
      default -> throw new IllegalArgumentException("Invalid period: " + period);
    }

    Long totalContentViews = getTotalContentViews(userId, startDate, endDate);
    Long totalMarketViews = getTotalMarketViews(userId, startDate, endDate);

    return DashboardViewStatsDTO.builder()
        .totalContentViews(totalContentViews)
        .totalMarketViews(totalMarketViews)
        .build();
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentTotalViewStatsDTO> getContentTotalViewStats(
      Long userId, String period, Pageable pageable) {
    LocalDate endDate;
    LocalDate startDate;

    // LAST_MONTH의 경우 endDate도 지난달 마지막 날로 설정
    switch (period) {
      case "TODAY" -> {
        startDate = LocalDate.now();
        endDate = LocalDate.now();
      }
      case "LAST_7_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(6);
      }
      case "LAST_30_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(29);
      }
      case "THIS_MONTH" -> {
        endDate = LocalDate.now();
        startDate = endDate.withDayOfMonth(1);
      }
      case "LAST_MONTH" -> {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        startDate = lastMonth.atDay(1);
        endDate = lastMonth.atEndOfMonth(); // 지난달 마지막 날
      }
      default -> throw new IllegalArgumentException("Invalid period: " + period);
    }

    Page<FlatContentTotalViewStatsDTO> page =
        contentViewStatsCustomRepository.findTotalViewsByPeriodTypeAndStatDateBetween(
            userId, PeriodType.DAILY, startDate, endDate, pageable);

    List<ContentTotalViewStatsDTO> items =
        page.getContent().stream()
            .map(this::toContentTotalViewStatsDTO)
            .collect(Collectors.toList());

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public PageResponse<MarketViewStatsDTO> getMarketViewStats(
      Long userId, String period, Pageable pageable) {
    Market market = userReader.getMarket(userId);
    LocalDate endDate;
    LocalDate startDate;

    // LAST_MONTH의 경우 endDate도 지난달 마지막 날로 설정
    switch (period) {
      case "TODAY" -> {
        startDate = LocalDate.now();
        endDate = LocalDate.now();
      }
      case "LAST_7_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(6);
      }
      case "LAST_30_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(29);
      }
      case "THIS_MONTH" -> {
        endDate = LocalDate.now();
        startDate = endDate.withDayOfMonth(1);
      }
      case "LAST_MONTH" -> {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        startDate = lastMonth.atDay(1);
        endDate = lastMonth.atEndOfMonth(); // 지난달 마지막 날
      }
      default -> throw new IllegalArgumentException("Invalid period: " + period);
    }

    Page<FlatMarketViewStatsDTO> page =
        marketViewStatsCustomRepository.findByMarketIdAndPeriodTypeAndStatDateBetween(
            market.getMarketLinkUrl(), PeriodType.DAILY, startDate, endDate, pageable);
    // 1. 전체 날짜 리스트 생성
    List<LocalDate> allDates =
        startDate
            .datesUntil(endDate.plusDays(1))
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

    // 2. DB에서 조회한 데이터를 Map으로 변환
    Map<LocalDate, FlatMarketViewStatsDTO> dataMap =
        page.getContent().stream()
            .collect(Collectors.toMap(FlatMarketViewStatsDTO::getViewDate, Function.identity()));

    // 3. 페이징 처리 (범위 체크 추가)
    int start = (int) pageable.getOffset();

    // 페이지 범위 초과 체크
    if (start >= allDates.size()) {
      Page<FlatMarketViewStatsDTO> emptyPage =
          new PageImpl<>(Collections.emptyList(), pageable, allDates.size());

      List<MarketViewStatsDTO> items = Collections.emptyList();

      PageResponse.MetaData meta =
          PageResponse.MetaData.builder()
              .sortBy(pageable.getSort().iterator().next().getProperty())
              .sortDirection(pageable.getSort().iterator().next().getDirection().name())
              .totalViews(0L)
              .build();

      return PageResponse.from(emptyPage, items, meta);
    }

    int end = Math.min(start + pageable.getPageSize(), allDates.size());
    List<LocalDate> pagedDates = allDates.subList(start, end);

    // 4. 모든 날짜에 대한 데이터 생성
    List<FlatMarketViewStatsDTO> completeData =
        pagedDates.stream()
            .map(
                date ->
                    dataMap.getOrDefault(
                        date,
                        FlatMarketViewStatsDTO.builder()
                            .viewDate(date)
                            .dayOfWeek("")
                            .viewCount(0L)
                            .build()))
            .collect(Collectors.toList());

    // 5. 새로운 Page 객체 생성
    Page<FlatMarketViewStatsDTO> completePage =
        new PageImpl<>(completeData, pageable, allDates.size());

    // 총 조회수 계산
    long totalViews =
        completePage.getContent().stream()
            .mapToLong(dto -> dto.getViewCount() != null ? dto.getViewCount() : 0L)
            .sum();

    List<MarketViewStatsDTO> items =
        completePage.getContent().stream()
            .map(this::toMarketViewStatsDTO)
            .collect(Collectors.toList());

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .totalViews(totalViews)
            .build();

    return PageResponse.from(completePage, items, meta);
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentViewStatsDTO> getContentViewStats(
      Long userId, Long contentId, String period, Pageable pageable) {

    Content content = contentReader.getContentById(contentId);
    LocalDate endDate;
    LocalDate startDate;

    // LAST_MONTH의 경우 endDate도 지난달 마지막 날로 설정
    switch (period) {
      case "TODAY" -> {
        startDate = LocalDate.now();
        endDate = LocalDate.now();
      }
      case "LAST_7_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(6);
      }
      case "LAST_30_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(29);
      }
      case "THIS_MONTH" -> {
        endDate = LocalDate.now();
        startDate = endDate.withDayOfMonth(1);
      }
      case "LAST_MONTH" -> {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        startDate = lastMonth.atDay(1);
        endDate = lastMonth.atEndOfMonth(); // 지난달 마지막 날
      }
      default -> throw new IllegalArgumentException("Invalid period: " + period);
    }

    Page<FlatContentViewStatsDTO> page =
        contentViewStatsCustomRepository.findByContentIdAndPeriodTypeAndStatDateBetween(
            contentId, PeriodType.DAILY, startDate, endDate, pageable);

    // 1. 전체 날짜 리스트 생성
    List<LocalDate> allDates =
        startDate
            .datesUntil(endDate.plusDays(1))
            .sorted(Comparator.reverseOrder()) // 내림차순 정렬
            .collect(Collectors.toList());

    // 2. DB에서 조회한 데이터를 Map으로 변환
    Map<LocalDate, FlatContentViewStatsDTO> dataMap =
        page.getContent().stream()
            .collect(Collectors.toMap(FlatContentViewStatsDTO::getViewDate, Function.identity()));

    // 3. 페이징 처리 (범위 체크 추가)
    int start = (int) pageable.getOffset();

    // 페이지 범위 초과 체크
    if (start >= allDates.size()) {
      Page<FlatContentViewStatsDTO> emptyPage =
          new PageImpl<>(Collections.emptyList(), pageable, allDates.size());

      List<ContentViewStatsDTO> items = Collections.emptyList();

      PageResponse.MetaData meta =
          PageResponse.MetaData.builder()
              .sortBy(pageable.getSort().iterator().next().getProperty())
              .sortDirection(pageable.getSort().iterator().next().getDirection().name())
              .totalViews(0L)
              .contentTitle(content.getTitle())
              .build();

      return PageResponse.from(emptyPage, items, meta);
    }

    int end = Math.min(start + pageable.getPageSize(), allDates.size());
    List<LocalDate> pagedDates = allDates.subList(start, end);

    // 4. 모든 날짜에 대한 데이터 생성 (없는 날짜는 0으로)
    List<FlatContentViewStatsDTO> completeData =
        pagedDates.stream()
            .map(
                date ->
                    dataMap.getOrDefault(
                        date,
                        FlatContentViewStatsDTO.builder()
                            .viewDate(date)
                            .dayOfWeek("") // toContentViewStatsDTO에서 계산하므로 빈 문자열
                            .viewCount(0L)
                            .build()))
            .collect(Collectors.toList());

    // 5. 새로운 Page 객체 생성
    Page<FlatContentViewStatsDTO> completePage =
        new PageImpl<>(completeData, pageable, allDates.size());

    // 총 조회수 계산 (completePage 사용)
    long totalViews =
        completePage.getContent().stream()
            .mapToLong(dto -> dto.getViewCount() != null ? dto.getViewCount() : 0L)
            .sum();

    List<ContentViewStatsDTO> items =
        completePage.getContent().stream()
            .map(this::toContentViewStatsDTO)
            .collect(Collectors.toList());

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .totalViews(totalViews)
            .contentTitle(content.getTitle())
            .build();

    return PageResponse.from(completePage, items, meta);
  }

  @Transactional(readOnly = true)
  public PageResponse<ReferrerStatsDTO> getContentReferrerStats(
      Long userId, Long contentId, String period, Pageable pageable) {
    LocalDate endDate;
    LocalDate startDate;

    // LAST_MONTH의 경우 endDate도 지난달 마지막 날로 설정
    switch (period) {
      case "TODAY" -> {
        startDate = LocalDate.now();
        endDate = LocalDate.now();
      }
      case "LAST_7_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(6);
      }
      case "LAST_30_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(29);
      }
      case "THIS_MONTH" -> {
        endDate = LocalDate.now();
        startDate = endDate.withDayOfMonth(1);
      }
      case "LAST_MONTH" -> {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        startDate = lastMonth.atDay(1);
        endDate = lastMonth.atEndOfMonth(); // 지난달 마지막 날
      }
      default -> throw new IllegalArgumentException("Invalid period: " + period);
    }

    Page<FlatReferrerStatsDTO> page =
        referrerTrackingQueryRepository.findContentReferrerStats(
            contentId, startDate, endDate, pageable);

    List<ReferrerStatsDTO> items =
        page.getContent().stream().map(this::toReferrerStatsDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public PageResponse<ReferrerStatsDTO> getMarketReferrerStats(
      Long userId, String period, Pageable pageable) {

    Market market = userReader.getMarket(userId);

    LocalDate endDate;
    LocalDate startDate;

    // LAST_MONTH의 경우 endDate도 지난달 마지막 날로 설정
    switch (period) {
      case "TODAY" -> {
        startDate = LocalDate.now();
        endDate = LocalDate.now();
      }
      case "LAST_7_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(6);
      }
      case "LAST_30_DAYS" -> {
        endDate = LocalDate.now();
        startDate = endDate.minusDays(29);
      }
      case "THIS_MONTH" -> {
        endDate = LocalDate.now();
        startDate = endDate.withDayOfMonth(1);
      }
      case "LAST_MONTH" -> {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        startDate = lastMonth.atDay(1);
        endDate = lastMonth.atEndOfMonth(); // 지난달 마지막 날
      }
      default -> throw new IllegalArgumentException("Invalid period: " + period);
    }

    Page<FlatReferrerStatsDTO> page =
        referrerTrackingQueryRepository.findMarketReferrerStats(
            market.getMarketLinkUrl(), startDate, endDate, pageable);

    List<ReferrerStatsDTO> items =
        page.getContent().stream().map(this::toReferrerStatsDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  private ReferrerStatsDTO toReferrerStatsDTO(FlatReferrerStatsDTO flatReferrerStatsDTO) {
    String referrerUrl = flatReferrerStatsDTO.getReferrerUrl();
    String referrerPath =
        StringUtils.hasText(flatReferrerStatsDTO.getReferrerPath())
            ? flatReferrerStatsDTO.getReferrerPath()
            : extractPathFromUrl(referrerUrl);

    String domainCandidate = flatReferrerStatsDTO.getReferrerDomain();
    if (!StringUtils.hasText(domainCandidate)) {
      domainCandidate = extractDomainFromUrl(referrerUrl);
    }

    String normalizedDomain = normalize(domainCandidate);
    String domainLower = lowerCase(normalizedDomain);

    String source = normalize(flatReferrerStatsDTO.getSource());
    String medium = normalize(flatReferrerStatsDTO.getMedium());
    String campaign = normalize(flatReferrerStatsDTO.getCampaign());
    String content = normalize(flatReferrerStatsDTO.getContent());
    String term = normalize(flatReferrerStatsDTO.getTerm());

    boolean isDirect =
        !StringUtils.hasText(domainLower) || "(direct)".equalsIgnoreCase(domainLower);
    boolean isInternal = ReferrerDomainUtils.isInternalDomain(domainLower);
    boolean external = StringUtils.hasText(domainLower) && !isInternal && !isDirect;

    String domainForResponse =
        isDirect
            ? "(direct)"
            : (StringUtils.hasText(normalizedDomain)
                ? normalizedDomain
                : extractDomainFromUrl(referrerUrl));

    String trafficType = resolveTrafficType(domainLower, medium, campaign, isDirect, external);
    String displayLabel =
        resolveDisplayLabel(
            domainForResponse, source, campaign, external, isDirect, trafficType, referrerUrl);

    if (!external && !isDirect && StringUtils.hasText(referrerPath)) {
      displayLabel = domainForResponse + referrerPath;
    }

    if (!StringUtils.hasText(trafficType)) {
      trafficType = isDirect ? "DIRECT" : (external ? "REFERRAL" : "INTERNAL");
    }
    if (!StringUtils.hasText(displayLabel)) {
      displayLabel =
          isDirect
              ? "직접 방문"
              : (StringUtils.hasText(domainForResponse) ? domainForResponse : "알 수 없음");
    }

    if (!StringUtils.hasText(domainForResponse)) {
      domainForResponse = isDirect ? "(direct)" : domainForResponse;
    }

    return ReferrerStatsDTO.builder()
        .referrerUrl(referrerUrl)
        .referrerDomain(domainForResponse)
        .referrerPath(referrerPath)
        .source(source)
        .medium(medium)
        .campaign(campaign)
        .content(content)
        .term(term)
        .trafficType(trafficType)
        .external(external)
        .displayLabel(displayLabel)
        .visitCount(flatReferrerStatsDTO.getVisitCount())
        .build();
  }

  private String resolveTrafficType(
      String domain, String medium, String campaign, boolean isDirect, boolean external) {
    if (isDirect) {
      return "DIRECT";
    }
    if (!external) {
      return "INTERNAL";
    }
    if (StringUtils.hasText(campaign)) {
      return "CAMPAIGN";
    }

    String mediumLower = lowerCase(medium);
    if (mediumLower != null) {
      if (mediumLower.contains("social") || mediumLower.contains("sns")) {
        return "SOCIAL";
      }
      if (mediumLower.contains("search")
          || mediumLower.contains("cpc")
          || mediumLower.contains("ppc")) {
        return "SEARCH";
      }
      if (mediumLower.contains("email")) {
        return "EMAIL";
      }
      if (mediumLower.contains("display") || mediumLower.contains("banner")) {
        return "DISPLAY";
      }
    }

    String domainLower = lowerCase(domain);
    if (domainLower != null) {
      if (isSocialDomain(domainLower)) {
        return "SOCIAL";
      }
      if (isSearchDomain(domainLower)) {
        return "SEARCH";
      }
    }

    return "REFERRAL";
  }

  private String resolveDisplayLabel(
      String domain,
      String source,
      String campaign,
      boolean external,
      boolean isDirect,
      String trafficType,
      String referrerUrl) {
    if (isDirect) {
      return "직접 방문";
    }
    if (!external) {
      return "내부 이동";
    }
    if (StringUtils.hasText(campaign)) {
      return campaign;
    }

    String prettySource = prettifyLabel(source);
    if (StringUtils.hasText(prettySource)) {
      return prettySource;
    }

    if (StringUtils.hasText(domain) && !"(direct)".equals(domain)) {
      return domain;
    }

    if (StringUtils.hasText(referrerUrl)) {
      return referrerUrl;
    }

    String prettyType = prettifyLabel(trafficType);
    return StringUtils.hasText(prettyType) ? prettyType : "알 수 없음";
  }

  private String normalize(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String extractDomainFromUrl(String url) {
    if (!StringUtils.hasText(url)) {
      return "(direct)";
    }
    try {
      String decoded;
      try {
        decoded = java.net.URLDecoder.decode(url, java.nio.charset.StandardCharsets.UTF_8);
      } catch (IllegalArgumentException ignore) {
        decoded = url;
      }
      String clean = decoded.replaceFirst("^https?://", "").replaceFirst("^www\\.", "");
      int slash = clean.indexOf('/');
      return (slash >= 0 ? clean.substring(0, slash) : clean).toLowerCase();
    } catch (Exception e) {
      return "(unknown)";
    }
  }

  private String extractPathFromUrl(String url) {
    if (!StringUtils.hasText(url)) {
      return null;
    }
    try {
      String decoded;
      try {
        decoded = java.net.URLDecoder.decode(url, java.nio.charset.StandardCharsets.UTF_8);
      } catch (IllegalArgumentException ignore) {
        decoded = url;
      }
      String clean = decoded.replaceFirst("^https?://", "");
      int slash = clean.indexOf('/');
      if (slash < 0) {
        return null;
      }
      String pathAndQuery = clean.substring(slash);
      int question = pathAndQuery.indexOf('?');
      return question >= 0 ? pathAndQuery.substring(0, question) : pathAndQuery;
    } catch (Exception e) {
      return null;
    }
  }

  private String lowerCase(String value) {
    return value == null ? null : value.toLowerCase();
  }

  private boolean isSocialDomain(String lowerDomain) {
    return SOCIAL_DOMAIN_KEYWORDS.stream().anyMatch(lowerDomain::contains);
  }

  private boolean isSearchDomain(String lowerDomain) {
    return SEARCH_DOMAIN_KEYWORDS.stream().anyMatch(lowerDomain::contains);
  }

  private String prettifyLabel(String value) {
    String normalized = normalize(value);
    if (normalized == null) {
      return null;
    }

    normalized = normalized.replace('_', ' ').trim();
    if (normalized.isEmpty()) {
      return null;
    }

    String[] parts = normalized.split("\\s+");
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      if (part.isEmpty()) {
        continue;
      }
      String lower = part.toLowerCase();
      builder.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
      if (i < parts.length - 1) {
        builder.append(' ');
      }
    }
    return builder.toString();
  }

  private MarketViewStatsDTO toMarketViewStatsDTO(FlatMarketViewStatsDTO flatMarketViewStatsDTO) {
    return MarketViewStatsDTO.builder()
        .viewDate(flatMarketViewStatsDTO.getViewDate())
        .dayOfWeek(
            flatMarketViewStatsDTO
                .getViewDate()
                .getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN))
        .viewCount(flatMarketViewStatsDTO.getViewCount())
        .build();
  }

  private ContentTotalViewStatsDTO toContentTotalViewStatsDTO(
      FlatContentTotalViewStatsDTO flatContentTotalViewStatsDTO) {
    return ContentTotalViewStatsDTO.builder()
        .contentId(flatContentTotalViewStatsDTO.getContentId())
        .contentTitle(flatContentTotalViewStatsDTO.getContentTitle())
        .totalViews(flatContentTotalViewStatsDTO.getTotalViews())
        .build();
  }

  private ContentViewStatsDTO toContentViewStatsDTO(
      FlatContentViewStatsDTO flatContentViewStatsDTO) {
    return ContentViewStatsDTO.builder()
        .viewDate(flatContentViewStatsDTO.getViewDate())
        .dayOfWeek(
            flatContentViewStatsDTO
                .getViewDate()
                .getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN))
        .viewCount(flatContentViewStatsDTO.getViewCount())
        .build();
  }

  // 총 컨텐츠 조회수
  private Long getTotalContentViews(Long sellerId, LocalDate startDate, LocalDate endDate) {
    List<Long> contentIds = contentReader.findIdsByUserId(sellerId);
    return contentViewStatsRepository.getTotalContentViews(contentIds, startDate, endDate);
  }

  // 총 마켓 조회수
  private Long getTotalMarketViews(Long sellerId, LocalDate startDate, LocalDate endDate) {
    return marketViewStatsRepository.getTotalMarketViews(sellerId, startDate, endDate);
  }

  private DashboardContentOverviewDTO toDashboardContentOverviewDTO(
      FlatContentOverviewDTO flatContentOverviewDTO) {
    return DashboardContentOverviewDTO.builder()
        .contentId(flatContentOverviewDTO.getContentId())
        .contentTitle(flatContentOverviewDTO.getContentTitle())
        .build();
  }
}
