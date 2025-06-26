package liaison.groble.application.content;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.content.dto.FlatAdminContentSummaryInfoDTO;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.repository.ContentCustomRepository;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentReader {
  private final ContentRepository contentRepository;
  private final ContentCustomRepository contentCustomRepository;

  public Optional<Content> findByUserAndIsRepresentativeTrue(User user) {
    return contentRepository.findByUserAndIsRepresentativeTrue(user);
  }

  // 활성 콘텐츠 보유 여부 확인
  public boolean hasActiveContent(User user) {
    return contentRepository.existsByUserAndStatus(user, ContentStatus.ACTIVE);
  }

  // ===== ID로 Content 조회 =====
  public Content getContentById(Long contentId) {
    return contentRepository
        .findById(contentId)
        .orElseThrow(() -> new EntityNotFoundException("콘텐츠를 찾을 수 없습니다. ID: " + contentId));
  }

  // ===== status & ID로 Content 조회 =====
  public Content getContentByStatusAndId(Long contentId, ContentStatus status) {
    return contentRepository
        .findByIdAndStatus(contentId, status)
        .orElseThrow(() -> new EntityNotFoundException("콘텐츠를 찾을 수 없습니다. ID: " + contentId));
  }

  public Content findByIdAndUser(Long contentId, User user) {
    return contentRepository
        .findByIdAndUser(contentId, user)
        .orElseThrow(() -> new EntityNotFoundException("콘텐츠를 찾을 수 없습니다. ID: " + contentId));
  }

  // ===== 메이커의 대표 Content 조회 =====
  public FlatContentPreviewDTO getRepresentativeContentByUser(User user) {
    return contentCustomRepository.findRepresentativeContentByUser(user).orElse(null);
  }

  // ===== 마켓 목록에 들어가는 모든 콘텐츠(페이지네이션) 조회 =====
  public Page<FlatContentPreviewDTO> findAllMarketContentsByUserId(Long userId, Pageable pageable) {
    return contentCustomRepository.findAllMarketContentsByUserId(userId, pageable);
  }

  public Page<FlatAdminContentSummaryInfoDTO> findContentsByPageable(Pageable pageable) {
    return contentCustomRepository.findContentsByPageable(pageable);
  }

  public boolean existsSellingContentByUser(Long userId) {
    return contentCustomRepository.existsSellingContentByUser(userId);
  }
}
