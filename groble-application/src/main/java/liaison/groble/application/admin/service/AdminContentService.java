package liaison.groble.application.admin.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import liaison.groble.application.admin.dto.AdminContentSummaryInfoDTO;
import liaison.groble.application.admin.dto.AdminDocumentFileInfoDTO;
import liaison.groble.application.content.ContentReader;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.application.notification.mapper.NotificationMapper;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatAdminContentSummaryInfoDTO;
import liaison.groble.domain.content.dto.FlatAdminDocumentFileDTO;
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
  private final NotificationService notificationService;
  private final KakaoNotificationService kakaoNotificationService;

  public PageResponse<AdminContentSummaryInfoDTO> getAllContents(Pageable pageable) {
    Page<FlatAdminContentSummaryInfoDTO> contentPage =
        contentReader.findContentsByPageable(pageable);

    List<Long> contentIds =
        contentPage.getContent().stream()
            .map(FlatAdminContentSummaryInfoDTO::getContentId)
            .toList();

    Map<Long, List<AdminDocumentFileInfoDTO>> documentFilesMap = buildDocumentFileMap(contentIds);

    List<AdminContentSummaryInfoDTO> items =
        contentPage.getContent().stream()
            .map(
                flat ->
                    convertFlatDTOToInfoResponse(
                        flat,
                        documentFilesMap.getOrDefault(
                            flat.getContentId(), Collections.emptyList())))
            .toList();

    PageResponse.MetaData meta = resolveSortMeta(pageable);

    return PageResponse.from(contentPage, items, meta);
  }

  public PageResponse<AdminContentSummaryInfoDTO> searchContentsByTitle(
      String titleKeyword, Pageable pageable) {
    if (!StringUtils.hasText(titleKeyword)) {
      throw new IllegalArgumentException("검색할 콘텐츠 제목을 입력해주세요.");
    }

    Page<FlatAdminContentSummaryInfoDTO> contentPage =
        contentReader.searchAdminContentsByTitle(titleKeyword, pageable);

    List<Long> contentIds =
        contentPage.getContent().stream()
            .map(FlatAdminContentSummaryInfoDTO::getContentId)
            .toList();
    Map<Long, List<AdminDocumentFileInfoDTO>> documentFilesMap = buildDocumentFileMap(contentIds);

    List<AdminContentSummaryInfoDTO> items =
        contentPage.getContent().stream()
            .map(
                flat ->
                    convertFlatDTOToInfoResponse(
                        flat,
                        documentFilesMap.getOrDefault(
                            flat.getContentId(), Collections.emptyList())))
            .toList();

    PageResponse.MetaData meta = resolveSortMeta(pageable);

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
    SellDetails sellDetails =
        SellDetails.builder()
            .contentId(content.getId())
            .thumbnailUrl(content.getThumbnailUrl())
            .build();

    notificationRepository.save(
        notificationMapper.toNotification(
            content.getUser().getId(),
            NotificationType.SELL,
            SubNotificationType.CONTENT_SOLD_STOPPED,
            sellDetails));

    kakaoNotificationService.sendNotification(
        KakaoNotificationDTO.builder()
            .type(KakaoNotificationType.CONTENT_DISCONTINUED)
            .phoneNumber(content.getUser().getPhoneNumber())
            .sellerName(content.getUser().getNickname())
            .contentTitle(content.getTitle())
            .contentId(content.getId())
            .build());
  }

  private AdminContentSummaryInfoDTO convertFlatDTOToInfoResponse(
      FlatAdminContentSummaryInfoDTO flat, List<AdminDocumentFileInfoDTO> documentFiles) {
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
        .isSearchExposed(flat.getIsSearchExposed())
        .documentFiles(documentFiles)
        .build();
  }

  private Map<Long, List<AdminDocumentFileInfoDTO>> buildDocumentFileMap(List<Long> contentIds) {
    if (contentIds == null || contentIds.isEmpty()) {
      return Collections.emptyMap();
    }
    List<FlatAdminDocumentFileDTO> flatFiles =
        contentReader.findDocumentFilesByContentIds(contentIds);

    return flatFiles.stream()
        .collect(
            Collectors.groupingBy(
                FlatAdminDocumentFileDTO::getContentId,
                Collectors.mapping(this::convertToDocumentFileInfoDTO, Collectors.toList())));
  }

  private AdminDocumentFileInfoDTO convertToDocumentFileInfoDTO(
      FlatAdminDocumentFileDTO documentFileDTO) {
    return AdminDocumentFileInfoDTO.builder()
        .optionId(documentFileDTO.getOptionId())
        .optionName(documentFileDTO.getOptionName())
        .documentOriginalFileName(documentFileDTO.getDocumentOriginalFileName())
        .documentFileUrl(documentFileDTO.getDocumentFileUrl())
        .build();
  }

  private PageResponse.MetaData resolveSortMeta(Pageable pageable) {
    if (pageable == null || pageable.getSort().isUnsorted()) {
      return PageResponse.MetaData.builder().build();
    }
    Sort.Order order = pageable.getSort().iterator().next();
    return PageResponse.MetaData.builder()
        .sortBy(order.getProperty())
        .sortDirection(order.getDirection().name())
        .build();
  }
}
