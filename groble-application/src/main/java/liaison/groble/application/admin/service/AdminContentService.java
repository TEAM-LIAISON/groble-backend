package liaison.groble.application.admin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.dto.ContentDto;
import liaison.groble.application.content.dto.ContentOptionDto;
import liaison.groble.application.notification.mapper.NotificationMapper;
import liaison.groble.domain.content.entity.CoachingOption;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.content.entity.DocumentOption;
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

  @Transactional
  public void approveContent(Long contentId) {
    Content content = contentReader.getContentById(contentId);
    content.setStatus(ContentStatus.VALIDATED);
    saveAndConvertToDto(content);

    ReviewDetails reviewDetails =
        ReviewDetails.builder()
            .contentId(content.getId())
            .thumbnailUrl(content.getThumbnailUrl())
            .build();

    notificationRepository.save(
        notificationMapper.toNotification(
            content.getUser().getId(),
            NotificationType.REVIEW,
            SubNotificationType.CONTENT_REVIEW_APPROVED,
            reviewDetails));
  }

  @Transactional
  public void rejectContent(Long contentId, String rejectReason) {

    Content content = contentReader.getContentById(contentId);

    content.setStatus(ContentStatus.REJECTED);
    content.setRejectReason(rejectReason);
    log.info("콘텐츠 심사 거절 완료. 콘텐츠 ID: {}", contentId);

    saveAndConvertToDto(content);

    ReviewDetails reviewDetails =
        ReviewDetails.builder()
            .contentId(content.getId())
            .thumbnailUrl(content.getThumbnailUrl())
            .build();

    notificationRepository.save(
        notificationMapper.toNotification(
            content.getUser().getId(),
            NotificationType.REVIEW,
            SubNotificationType.CONTENT_REVIEW_REJECTED,
            reviewDetails));
  }

  /** Content를 저장하고 DTO로 변환합니다. */
  private ContentDto saveAndConvertToDto(Content content) {
    content = contentRepository.save(content);
    log.info("콘텐츠 저장 완료. ID: {}, 유저 ID: {}", content.getId(), content.getUser().getId());
    return convertToDto(content);
  }

  /** Content를 DTO로 변환합니다. */
  private ContentDto convertToDto(Content content) {
    // null 체크
    if (content == null) {
      return null;
    }

    // 옵션 변환
    List<ContentOptionDto> optionDtos = new ArrayList<>();
    if (content.getOptions() != null) {
      for (ContentOption option : content.getOptions()) {
        if (option == null) continue;

        ContentOptionDto.ContentOptionDtoBuilder builder =
            ContentOptionDto.builder()
                .contentOptionId(option.getId())
                .name(option.getName())
                .description(option.getDescription())
                .price(option.getPrice());

        if (option instanceof CoachingOption coachingOption) {
          builder
              .coachingPeriod(
                  coachingOption.getCoachingPeriod() != null
                      ? coachingOption.getCoachingPeriod().name()
                      : null)
              .documentProvision(
                  coachingOption.getDocumentProvision() != null
                      ? coachingOption.getDocumentProvision().name()
                      : null)
              .coachingType(
                  coachingOption.getCoachingType() != null
                      ? coachingOption.getCoachingType().name()
                      : null)
              .coachingTypeDescription(coachingOption.getCoachingTypeDescription());
        } else if (option instanceof DocumentOption documentOption) {
          builder
              .contentDeliveryMethod(
                  documentOption.getContentDeliveryMethod() != null
                      ? documentOption.getContentDeliveryMethod().name()
                      : null)
              .documentFileUrl(
                  documentOption.getDocumentFileUrl() != null
                      ? documentOption.getDocumentFileUrl()
                      : null)
              .documentLinkUrl(
                  documentOption.getDocumentLinkUrl() != null
                      ? documentOption.getDocumentLinkUrl()
                      : null);
        }

        optionDtos.add(builder.build());
      }
    }

    // Content DTO 구성
    ContentDto.ContentDtoBuilder dtoBuilder =
        ContentDto.builder()
            .contentId(content.getId())
            .title(content.getTitle())
            .thumbnailUrl(content.getThumbnailUrl())
            .options(optionDtos.isEmpty() ? null : optionDtos);

    // Enum을 안전하게 문자열로 변환
    if (content.getContentType() != null) {
      dtoBuilder.contentType(content.getContentType().name());
    }

    if (content.getStatus() != null) {
      dtoBuilder.status(content.getStatus().name());
    } else {
      dtoBuilder.status(ContentStatus.DRAFT.name()); // 기본값
    }

    // 카테고리가 null이 아닌 경우에만 ID 설정
    if (content.getCategory() != null) {
      dtoBuilder.categoryId(content.getCategory().getCode());
    }

    // 콘텐츠 소개와 상세 이미지 URL 추가
    if (content.getContentIntroduction() != null) {
      dtoBuilder.contentIntroduction(content.getContentIntroduction());
    }

    if (content.getServiceTarget() != null) {
      dtoBuilder.serviceTarget(content.getServiceTarget());
    }

    if (content.getServiceProcess() != null) {
      dtoBuilder.serviceProcess(content.getServiceProcess());
    }

    if (content.getMakerIntro() != null) {
      dtoBuilder.makerIntro(content.getMakerIntro());
    }

    return dtoBuilder.build();
  }
}
