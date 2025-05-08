package liaison.groble.application.scrap.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.scrap.dto.ContentScrapDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.InvalidRequestException;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.scrap.entity.ContentScrap;
import liaison.groble.domain.scrap.repository.ContentScrapRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentScrapService {

  private final ContentScrapRepository contentScrapRepository;
  private final UserReader userReader;
  private final ContentReader contentReader;

  @Transactional
  public ContentScrapDto updateContentScrap(Long userId, Long contentId, boolean changeScrapValue) {
    boolean scrapExists = contentScrapRepository.existsByUserIdAndContentId(userId, contentId);

    if (scrapExists) {
      handleExistingScrap(userId, contentId, changeScrapValue);
    } else {
      handleNonExistingScrap(userId, contentId, changeScrapValue);
    }

    return ContentScrapDto.builder().contentId(contentId).isContentScrap(!scrapExists).build();
  }

  private void handleExistingScrap(Long userId, Long contentId, boolean changeScrapValue) {
    if (!changeScrapValue) {
      contentScrapRepository.deleteByUserIdAndContentId(userId, contentId);
    } else {
      throw new IllegalArgumentException("Already scrapped");
    }
  }

  private void handleNonExistingScrap(Long userId, Long contentId, boolean changeScrapValue) {
    if (changeScrapValue) {
      User user = userReader.getUserById(userId);
      Content content = contentReader.getContentById(contentId);
      ContentScrap contentScrap = new ContentScrap(null, user, content);
      contentScrapRepository.save(contentScrap);
    } else {
      throw new InvalidRequestException("유효하지 않은 스크랩 요청입니다.");
    }
  }
}
