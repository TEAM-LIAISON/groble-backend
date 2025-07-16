package liaison.groble.application.content;

import java.util.List;
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

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentReader {
  private final ContentRepository contentRepository;
  private final ContentCustomRepository contentCustomRepository;

  // ===== 특정 사용자의 대표 콘텐츠를 조회합니다. =====
  public Optional<Content> findByUserAndIsRepresentativeTrue(User user) {
    return contentRepository.findByUserAndIsRepresentativeTrue(user);
  }

  // ===== 특정 사용자가 판매중인 콘텐츠를 보유하고 있는지 조회합니다.  =====
  public boolean hasActiveContent(User user) {
    return contentRepository.existsByUserAndStatus(user, ContentStatus.ACTIVE);
  }

  // ===== 특정 콘텐츠 ID로 콘텐츠 조회 =====
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
  public FlatContentPreviewDTO getRepresentativeContentByUser(Long userId) {
    return contentCustomRepository.findRepresentativeContentByUser(userId).orElse(null);
  }

  // ===== 마켓 목록에 들어가는 모든 콘텐츠(페이지네이션) 조회 =====
  public Page<FlatContentPreviewDTO> findAllMarketContentsByUserIdWithPaging(
      Long userId, Pageable pageable) {
    return contentCustomRepository.findAllMarketContentsByUserIdWithPaging(userId, pageable);
  }

  public List<FlatContentPreviewDTO> findAllMarketContentsByUserId(Long userId) {
    return contentCustomRepository.findAllMarketContentsByUserId(userId);
  }

  public Page<FlatAdminContentSummaryInfoDTO> findContentsByPageable(Pageable pageable) {
    return contentCustomRepository.findContentsByPageable(pageable);
  }

  public Page<FlatContentPreviewDTO> findMyContentsWithStatus(
      Pageable pageable, Long userId, List<ContentStatus> contentStatuses) {
    return contentCustomRepository.findMyContentsWithStatus(pageable, userId, contentStatuses);
  }

  public boolean existsSellingContentByUser(Long userId) {
    return contentCustomRepository.existsSellingContentByUser(userId);
  }
}
