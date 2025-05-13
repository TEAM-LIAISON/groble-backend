package liaison.groble.application.scrap.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.scrap.dto.ContentScrapCardDto;
import liaison.groble.application.scrap.dto.ContentScrapDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.InvalidRequestException;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.dto.FlatScrapContentPreviewDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.scrap.entity.ContentScrap;
import liaison.groble.domain.scrap.repository.ContentScrapRepository;
import liaison.groble.domain.user.entity.User;
import liaison.groble.persistence.scrap.ContentScrapCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentScrapService {

  private final ContentScrapRepository contentScrapRepository;
  private final ContentScrapCustomRepository contentScrapCustomRepository;
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

  @Transactional(readOnly = true)
  public CursorResponse<ContentScrapCardDto> getMyScrapContents(
      Long userId, String cursor, int size, String type) {
    Long lastContentId = parseContentIdFromCursor(cursor);
    ContentType contentType = parseContentType(type);

    CursorResponse<FlatScrapContentPreviewDTO> flatScrapContentDtos =
        contentScrapCustomRepository.getMyScrapContentsWithCursor(
            userId, lastContentId, size, contentType);

    List<ContentScrapCardDto> contentScrapCardDtos =
        flatScrapContentDtos.getItems().stream().map(this::convertFlatDtoToCardDto).toList();

    int totalCount = contentScrapCustomRepository.countMyScrapContents(userId, contentType);

    // 7. 응답 구성
    return CursorResponse.<ContentScrapCardDto>builder()
        .items(contentScrapCardDtos)
        .nextCursor(flatScrapContentDtos.getNextCursor())
        .hasNext(flatScrapContentDtos.isHasNext())
        .totalCount(totalCount)
        .meta(flatScrapContentDtos.getMeta())
        .build();
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

  private Long parseContentIdFromCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }

    try {
      return Long.parseLong(cursor);
    } catch (NumberFormatException e) {
      log.warn("유효하지 않은 커서 형식: {}", cursor);
      return null;
    }
  }

  private ContentType parseContentType(String type) {
    if (type == null || type.isBlank()) {
      return null;
    }

    try {
      return ContentType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("유효하지 않은 콘텐츠 유형: {}", type);
      return null;
    }
  }

  /** FlatPreviewContentDTO를 ContentCardDto로 변환합니다. */
  private ContentScrapCardDto convertFlatDtoToCardDto(FlatScrapContentPreviewDTO flat) {
    return ContentScrapCardDto.builder()
        .contentId(flat.getContentId())
        .contentType(flat.getContentType())
        .title(flat.getTitle())
        .thumbnailUrl(flat.getThumbnailUrl())
        .sellerName(flat.getSellerName())
        .isContentScrap(flat.getIsContentScrap())
        .build();
  }
}
