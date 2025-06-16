package liaison.groble.application.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dto.AdminContentSummaryInfoDto;
import liaison.groble.application.admin.mapper.ContentEntityMapper;
import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.dto.ContentDto;
import liaison.groble.application.notification.mapper.NotificationMapper;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatAdminContentSummaryInfoDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.notification.entity.ReviewDetails;
import liaison.groble.domain.notification.enums.NotificationType;
import liaison.groble.domain.notification.enums.SubNotificationType;
import liaison.groble.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminContentService {

  private final ContentReader contentReader;
  private final ContentRepository contentRepository;
  private final NotificationMapper notificationMapper;
  private final NotificationRepository notificationRepository;
  private final ContentEntityMapper contentEntityMapper;

  public PageResponse<AdminContentSummaryInfoDto> getAllContents(Pageable pageable) {
    Page<FlatAdminContentSummaryInfoDTO> contentPage =
        contentReader.findContentsByPageable(pageable);

    List<AdminContentSummaryInfoDto> items =
        contentPage.getContent().stream().map(this::convertFlatDtoToInfoResponse).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(contentPage, items, meta);
  }

  @Transactional
  public void approveContent(Long contentId) {
    Content content = contentReader.getContentById(contentId);
    content.setStatus(ContentStatus.VALIDATED);
    saveAndConvertToDto(content);

    sendContentReviewNotification(content, SubNotificationType.CONTENT_REVIEW_APPROVED);
  }

  @Transactional
  public void rejectContent(Long contentId, String rejectReason) {
    Content content = contentReader.getContentById(contentId);
    content.setStatus(ContentStatus.REJECTED);
    content.setRejectReason(rejectReason);

    log.info("콘텐츠 심사 거절 완료. 콘텐츠 ID: {}", contentId);
    saveAndConvertToDto(content);

    sendContentReviewNotification(content, SubNotificationType.CONTENT_REVIEW_REJECTED);
  }

  private ContentDto saveAndConvertToDto(Content content) {
    content = contentRepository.save(content);
    log.info("콘텐츠 저장 완료. ID: {}, 유저 ID: {}", content.getId(), content.getUser().getId());
    return contentEntityMapper.toDto(content);
  }

  private void sendContentReviewNotification(Content content, SubNotificationType subType) {
    ReviewDetails reviewDetails =
        ReviewDetails.builder()
            .contentId(content.getId())
            .thumbnailUrl(content.getThumbnailUrl())
            .build();

    notificationRepository.save(
        notificationMapper.toNotification(
            content.getUser().getId(), NotificationType.REVIEW, subType, reviewDetails));
  }

  private AdminContentSummaryInfoDto convertFlatDtoToInfoResponse(
      FlatAdminContentSummaryInfoDTO flat) {
    return AdminContentSummaryInfoDto.builder()
        .contentId(flat.getContentId())
        .createdAt(flat.getCreatedAt())
        .contentType(flat.getContentType())
        .sellerName(flat.getSellerName())
        .contentTitle(flat.getContentTitle())
        .priceOptionLength(flat.getPriceOptionLength())
        .minPrice(flat.getMinPrice())
        .contentStatus(flat.getContentStatus())
        .adminContentCheckingStatus(flat.getAdminContentCheckingStatus())
        .build();
  }
}
