package liaison.groble.application.content.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.dto.ContentViewCountDTO;
import liaison.groble.application.dashboard.service.ViewTrackingKeyGenerator;
import liaison.groble.application.dashboard.service.ViewTrackingKeyGenerator.ViewerIdentity;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.dashboard.entity.ContentViewLog;
import liaison.groble.domain.dashboard.repository.ContentViewLogRepository;
import liaison.groble.domain.port.DailyViewPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentViewCountService {
  // 관리자 계정 ID 상수
  private static final Long ADMIN_USER_ID = 1L;

  // Repository
  private final ContentViewLogRepository contentViewLogRepository;

  // Reader
  private final ContentReader contentReader;

  // Port
  private final DailyViewPort dailyViewPort;
  private final ViewTrackingKeyGenerator viewTrackingKeyGenerator;

  @Async
  public void recordContentView(Long contentId, ContentViewCountDTO contentViewCountDTO) {
    // 관리자 계정(groble@groble.im, userId=1)에 대해서는 조회수 집계를 하지 않음
    if (ADMIN_USER_ID.equals(contentViewCountDTO.getUserId())) {
      return;
    }

    // 관리자 페이지(admin.groble.im)에서의 유입은 조회수 집계를 하지 않음
    String referer = contentViewCountDTO.getReferer();
    if (referer != null && referer.contains("admin.groble.im")) {
      log.debug("Skipping content view counting for admin page access. contentId={}", contentId);
      return;
    }

    Long viewerId = contentViewCountDTO.getUserId();
    if (viewerId != null) {
      try {
        Content content = contentReader.getContentWithSeller(contentId);
        if (content.getUser() != null && viewerId.equals(content.getUser().getId())) {
          log.debug(
              "Skipping content view counting for owner access. contentId={}, userId={}",
              contentId,
              viewerId);
          return;
        }
      } catch (Exception e) {
        log.warn(
            "Could not verify content ownership during view counting. contentId={}, userId={}",
            contentId,
            viewerId,
            e);
      }
    }
    // # 일별 조회수
    // view:count:content:123:20250128 → "42"

    // # 중복 방지 (5분)
    // viewed:content:123:user:456 → "1"
    // viewed:content:123:ip:192.168.1.1:382910 → "1"
    ViewerIdentity identity =
        viewTrackingKeyGenerator.generate(
            contentViewCountDTO.getUserId(),
            contentViewCountDTO.getIp(),
            contentViewCountDTO.getUserAgent());

    if (dailyViewPort.incrementViewIfNotDuplicate("content", contentId, identity.viewerKey())) {
      // 로그 저장
      ContentViewLog log =
          ContentViewLog.builder()
              .contentId(contentId)
              .viewerId(contentViewCountDTO.getUserId())
              .viewerIp(identity.normalizedIp())
              .userAgent(identity.normalizedUserAgent())
              .visitorHash(identity.visitorHash())
              .viewedAt(LocalDateTime.now())
              .build();

      contentViewLogRepository.save(log);
    }
  }
}
