package liaison.groble.application.content;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.repository.ContentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentReader {
  private final ContentRepository contentRepository;

  // ===== ID로 Content 조회 =====
  public Content getContentById(Long contentId) {
    return contentRepository
        .findById(contentId)
        .orElseThrow(() -> new EntityNotFoundException("컨텐츠를 찾을 수 없습니다. ID: " + contentId));
  }
}
