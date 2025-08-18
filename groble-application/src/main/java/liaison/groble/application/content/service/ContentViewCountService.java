package liaison.groble.application.content.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import liaison.groble.application.content.dto.ContentViewCountDTO;
import liaison.groble.domain.dashboard.entity.ContentViewLog;
import liaison.groble.domain.dashboard.repository.ContentViewLogRepository;
import liaison.groble.domain.port.DailyViewPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentViewCountService {
  // Repository
  private final ContentViewLogRepository contentViewLogRepository;

  // Port
  private final DailyViewPort dailyViewPort;

  @Async
  public void recordContentView(Long contentId, ContentViewCountDTO contentViewCountDTO) {
    // # 일별 조회수
    // view:count:content:123:20250128 → "42"

    // # 중복 방지 (1시간)
    // viewed:content:123:user:456 → "1"
    // viewed:content:123:ip:192.168.1.1:382910 → "1"
    String viewerKey =
        contentViewCountDTO.getUserId() != null
            ? "user:" + contentViewCountDTO.getUserId()
            : "ip:"
                + contentViewCountDTO.getIp()
                + ":"
                + contentViewCountDTO.getUserAgent().hashCode();

    if (dailyViewPort.incrementViewIfNotDuplicate("content", contentId, viewerKey)) {
      // 로그 저장
      ContentViewLog log =
          ContentViewLog.builder()
              .contentId(contentId)
              .viewerId(contentViewCountDTO.getUserId())
              .viewerIp(contentViewCountDTO.getIp())
              .userAgent(contentViewCountDTO.getUserAgent())
              .referer(contentViewCountDTO.getReferer())
              .viewedAt(LocalDateTime.now())
              .build();

      contentViewLogRepository.save(log);
    }
  }
}
