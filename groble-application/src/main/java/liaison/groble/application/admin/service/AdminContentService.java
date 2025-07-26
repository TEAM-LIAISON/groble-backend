package liaison.groble.application.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dto.AdminContentSummaryInfoDTO;
import liaison.groble.application.content.ContentReader;
import liaison.groble.application.notification.mapper.NotificationMapper;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatAdminContentSummaryInfoDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.AdminContentCheckingStatus;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.notification.entity.detail.SellDetails;
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

  public PageResponse<AdminContentSummaryInfoDTO> getAllContents(Pageable pageable) {
    Page<FlatAdminContentSummaryInfoDTO> contentPage =
        contentReader.findContentsByPageable(pageable);

    List<AdminContentSummaryInfoDTO> items =
        contentPage.getContent().stream().map(this::convertFlatDTOToInfoResponse).toList();

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
    content.setAdminContentCheckingStatus(AdminContentCheckingStatus.VALIDATED);
    contentRepository.save(content);
  }

  @Transactional
  public void rejectContent(Long contentId, String rejectReason) {
    Content content = contentReader.getContentById(contentId);
    content.setAdminContentCheckingStatus(AdminContentCheckingStatus.REJECTED);
    content.setStatus(ContentStatus.DISCONTINUED);
    content.setRejectReason(rejectReason);
    contentRepository.save(content);

    sendContentSoldNotification(content);
  }

  private void sendContentSoldNotification(Content content) {
    SellDetails sellDetails = SellDetails.builder().contentId(content.getId()).build();

    notificationRepository.save(
        notificationMapper.toNotification(
            content.getUser().getId(),
            NotificationType.SELL,
            SubNotificationType.CONTENT_SOLD_STOPPED,
            sellDetails));
  }

  private AdminContentSummaryInfoDTO convertFlatDTOToInfoResponse(
      FlatAdminContentSummaryInfoDTO flat) {
    return AdminContentSummaryInfoDTO.builder()
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
