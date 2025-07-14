package liaison.groble.application.content;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.repository.ContentReviewCustomRepository;
import liaison.groble.domain.content.repository.ContentReviewRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentReplyReader {
  private final ContentReviewRepository contentReviewRepository;
  private final ContentReviewCustomRepository contentReviewCustomRepository;
}
