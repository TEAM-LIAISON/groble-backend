package liaison.groble.application.notification.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import liaison.groble.application.notification.dto.NotificationDetailsDTO;
import liaison.groble.application.notification.dto.NotificationItemDTO;
import liaison.groble.application.notification.dto.NotificationItemsDTO;
import liaison.groble.application.notification.mapper.NotificationMapper;
import liaison.groble.domain.notification.entity.Notification;
import liaison.groble.domain.notification.entity.detail.CertifyDetails;
import liaison.groble.domain.notification.entity.detail.PurchaseDetails;
import liaison.groble.domain.notification.entity.detail.ReviewDetails;
import liaison.groble.domain.notification.entity.detail.SellDetails;
import liaison.groble.domain.notification.entity.detail.SystemDetails;
import liaison.groble.domain.notification.enums.NotificationType;
import liaison.groble.domain.notification.enums.SubNotificationType;
import liaison.groble.domain.notification.repository.NotificationCustomRepository;
import liaison.groble.domain.notification.repository.NotificationRepository;
import liaison.groble.domain.user.entity.User;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;
import liaison.groble.external.infotalk.dto.message.MessageResponse;
import liaison.groble.external.infotalk.service.BizppurioMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  @Value("${bizppurio.kakao-sender-key}")
  private String kakaoSenderKey; // 카카오톡 발신프로필키

  // 설정 파일에서 템플릿 정보를 가져옵니다
  // 이렇게 하면 템플릿이 변경되어도 코드 수정 없이 설정만 변경하면 됩니다
  @Value("${bizppurio.templates.welcome.code}")
  private String welcomeTemplateCode;

  @Value("${bizppurio.templates.purchase-complete.code}")
  private String purchaseCompleteTemplateCode;

  @Value("${bizppurio.templates.sale-complete.code}")
  private String saleCompleteTemplateCode;

  @Value("${bizppurio.templates.verification-complete.code}")
  private String verificationCompleteTemplateCode;

  @Value("${bizppurio.templates.review-register.code}")
  private String reviewRegisteredTemplateCode;

  @Value("${bizppurio.templates.content-discontinued.code}")
  private String contentDiscontinuedTemplateCode;

  private final NotificationCustomRepository notificationCustomRepository;
  private final NotificationRepository notificationRepository;
  private final NotificationReader notificationReader;
  private final NotificationMapper notificationMapper;

  // 카카오 알림톡 발송 서비스
  private final BizppurioMessageService messageService;

  public NotificationItemsDTO getNotificationItems(final Long userId) {
    List<Notification> notifications =
        notificationCustomRepository.getNotificationsByReceiverUser(userId);

    // Convert notifications to NotificationItemDTO list
    List<NotificationItemDTO> notificationItemDTOS =
        notifications.stream().map(this::toNotificationItemDTO).toList();

    // Build and return NotificationItemsDTO
    return NotificationItemsDTO.builder().notificationItems(notificationItemDTOS).build();
  }

  /** 사용자의 모든 알림을 삭제합니다. */
  @Transactional
  public void deleteAllNotifications(final Long userId) {
    notificationCustomRepository.deleteAllNotificationsByReceiverUser(userId);
    log.info("모든 알림이 삭제되었습니다. userId: {}", userId);
  }

  /** 특정 알림을 삭제합니다. */
  @Transactional
  public void deleteNotification(final Long userId, final Long notificationId) {
    notificationCustomRepository.deleteNotificationByReceiverUser(userId, notificationId);
    log.info("알림이 삭제되었습니다. userId: {}, notificationId: {}", userId, notificationId);
  }

  /** Converts a Notification entity to a NotificationItemDTO */
  private NotificationItemDTO toNotificationItemDTO(final Notification notification) {
    // 도메인 enum을 String으로 변환
    return NotificationItemDTO.builder()
        .notificationId(notification.getId())
        .notificationType(notification.getNotificationType().name()) // enum을 String으로 변환
        .subNotificationType(notification.getSubNotificationType().name())
        .notificationReadStatus(notification.getNotificationReadStatus().name())
        .notificationOccurTime(notification.getCreatedAt())
        .notificationDetails(
            createNotificationDetails(
                notification,
                notification.getNotificationType(),
                notification.getSubNotificationType()))
        .build();
  }

  /** Creates appropriate NotificationDetailsDTO based on notification type and subtype */
  private NotificationDetailsDTO createNotificationDetails(
      final Notification notification,
      final NotificationType type,
      final SubNotificationType subType) {

    // Switch based on notification type and subtype to create appropriate details
    return switch (type) {
      case CERTIFY -> createCertifyDetails(notification, subType);
      case REVIEW -> createReviewDetails(notification, subType);
      case SYSTEM -> createSystemDetails(notification, subType);
      case PURCHASE -> createPurchaseDetails(notification, subType);
      case SELL -> createSellDetails(notification, subType);
      default -> null;
    };
  }

  private NotificationDetailsDTO createCertifyDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.MAKER_CERTIFIED) {
      return NotificationDetailsDTO.makerCertified(notification.getCertifyDetails().getNickname());
    } else if (subNotificationType == SubNotificationType.MAKER_CERTIFY_REJECTED) {
      return NotificationDetailsDTO.makerCertifyRejected(
          notification.getCertifyDetails().getNickname());
    }
    return null;
  }

  private NotificationDetailsDTO createReviewDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.CONTENT_REVIEWED) {
      return NotificationDetailsDTO.contentReviewed(
          notification.getReviewDetails().getContentId(),
          notification.getReviewDetails().getReviewId(),
          notification.getReviewDetails().getThumbnailUrl());
    }
    return null;
  }

  private NotificationDetailsDTO createSystemDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.WELCOME_GROBLE) {
      return NotificationDetailsDTO.welcomeGroble(
          notification.getSystemDetails().getNickname(),
          notification.getSystemDetails().getSystemTitle());
    }
    return null;
  }

  private NotificationDetailsDTO createPurchaseDetails(
      Notification notification, SubNotificationType subNotificationType) {
    if (subNotificationType == SubNotificationType.CONTENT_REVIEW_REPLY) {
      return NotificationDetailsDTO.contentReviewReplied(
          notification.getPurchaseDetails().getContentId(),
          notification.getPurchaseDetails().getReviewId(),
          notification.getPurchaseDetails().getThumbnailUrl());
    } else if (subNotificationType == SubNotificationType.CONTENT_PURCHASED) {
      return NotificationDetailsDTO.contentPurchased(
          notification.getPurchaseDetails().getContentId(),
          notification.getPurchaseDetails().getMerchantUid(),
          notification.getPurchaseDetails().getThumbnailUrl());
    }
    return null;
  }

  private NotificationDetailsDTO createSellDetails(
      Notification notification, SubNotificationType subNotificationType) {
    // 콘텐츠 판매 [✅ 상품이 판매됐어요]
    if (subNotificationType == SubNotificationType.CONTENT_SOLD) {
      return NotificationDetailsDTO.contentSold(
          notification.getSellDetails().getContentId(),
          notification.getSellDetails().getPurchaseId(),
          notification.getSellDetails().getThumbnailUrl());
    }
    // 콘텐츠 판매 중단 [✅ 상품 판매가 중단됐어요]
    else if (subNotificationType == SubNotificationType.CONTENT_SOLD_STOPPED) {
      return NotificationDetailsDTO.contentSoldStopped(
          notification.getSellDetails().getContentId(),
          notification.getSellDetails().getThumbnailUrl());
    }
    return null;
  }

  @Transactional
  public void sendWelcomeNotification(User user) {
    SystemDetails systemDetails =
        SystemDetails.welcomeGroble(user.getNickname(), "그로블에 오신 것을 환영합니다!");

    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.SYSTEM,
            SubNotificationType.WELCOME_GROBLE,
            systemDetails);

    notificationRepository.save(notification);
  }

  @Transactional
  public void sendMakerCertifiedVerificationNotification(User user) {
    CertifyDetails certifyDetails = CertifyDetails.builder().nickname(user.getNickname()).build();

    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.CERTIFY,
            SubNotificationType.MAKER_CERTIFIED,
            certifyDetails);

    notificationRepository.save(notification);
    log.info("메이커 인증 승인 알림 발송: userId={}", user.getId());
  }

  @Transactional
  public void sendMakerRejectedVerificationNotificationAsync(Long userId, String nickname) {
    CertifyDetails certifyDetails = CertifyDetails.builder().nickname(nickname).build();

    Notification notification =
        notificationMapper.toNotification(
            userId,
            NotificationType.CERTIFY,
            SubNotificationType.MAKER_CERTIFY_REJECTED,
            certifyDetails);

    notificationRepository.save(notification);
  }

  @Transactional
  public void sendContentSoldNotification(
      User user, Long contentId, Long purchaseId, String thumbnailUrl) {
    SellDetails sellDetails =
        SellDetails.builder()
            .contentId(contentId)
            .purchaseId(purchaseId)
            .thumbnailUrl(thumbnailUrl)
            .build();

    Notification notification =
        notificationMapper.toNotification(
            user.getId(), NotificationType.SELL, SubNotificationType.CONTENT_SOLD, sellDetails);
    notificationRepository.save(notification);
  }

  @Transactional
  public void sendContentPurchasedNotification(
      User user, Long contentId, String merchantUid, String thumbnailUrl) {
    PurchaseDetails purchaseDetails =
        PurchaseDetails.builder()
            .contentId(contentId)
            .merchantUid(merchantUid)
            .thumbnailUrl(thumbnailUrl)
            .build();
    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.PURCHASE,
            SubNotificationType.CONTENT_PURCHASED,
            purchaseDetails);
    notificationRepository.save(notification);
  }

  @Transactional
  public void sendContentReviewReplyNotification(
      User user, Long contentId, Long reviewId, String thumbnailUrl) {
    PurchaseDetails purchaseDetails =
        PurchaseDetails.builder()
            .contentId(contentId)
            .reviewId(reviewId)
            .thumbnailUrl(thumbnailUrl)
            .build();

    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.PURCHASE,
            SubNotificationType.CONTENT_REVIEW_REPLY,
            purchaseDetails);

    notificationRepository.save(notification);
    log.info("콘텐츠 리뷰 답글 알림 발송: userId={}, contentId={}", user.getId(), contentId);
  }

  @Transactional
  public void sendContentReviewNotification(
      User user, Long contentId, Long reviewId, String thumbnailUrl) {
    ReviewDetails reviewDetails =
        ReviewDetails.builder()
            .contentId(contentId)
            .reviewId(reviewId)
            .thumbnailUrl(thumbnailUrl)
            .build();

    Notification notification =
        notificationMapper.toNotification(
            user.getId(),
            NotificationType.REVIEW,
            SubNotificationType.CONTENT_REVIEWED,
            reviewDetails);

    notificationRepository.save(notification);
    log.info("콘텐츠 리뷰 알림 발송: userId={}, contentId={}", user.getId(), contentId);
  }

  @Transactional
  public void readNotification(Long userId, Long notificationId) {
    Notification notification =
        notificationReader.getNotificationByIdAndUserId(notificationId, userId);
    notification.markAsRead();
  }

  // 카카오 알림톡 관련 메서드
  /** 2. 구매자 - 결제 알림 */
  public void sendPurchaseCompleteMessage(
      String phoneNumber,
      String buyerName,
      String contentTitle,
      BigDecimal price,
      String merchantUid) {
    try {
      String messageContent = null;
      String title = "[Groble] 결제 알림";
      // 3) 주문 상세 URL (경로 세그먼트 안전 인코딩)
      String orderUrl =
          UriComponentsBuilder.fromHttpUrl("https://www.groble.im")
              .path("/manage/purchase/{merchantUid}")
              .buildAndExpand(merchantUid)
              .encode()
              .toUriString();

      List<ButtonInfo> buttons =
          Arrays.asList(
              ButtonInfo.builder()
                  .name("내 콘텐츠 보러 가기")
                  .type("WL") // 웹링크
                  .urlMobile(orderUrl)
                  .urlPc(orderUrl)
                  .build());
      log.info("구매 완료 알림톡 발송 시작 - 구매자: {}, 템플릿코드: {}", buyerName, purchaseCompleteTemplateCode);

      // 알림톡 발송
      MessageResponse response =
          messageService.sendAlimtalk(
              phoneNumber,
              purchaseCompleteTemplateCode,
              title,
              messageContent,
              kakaoSenderKey,
              buttons);

      if (response.isSuccess()) {
        log.info("구매 완료 메시지 발송 성공 - 구매자: {}, 메시지키: {}", buyerName, response.getMessageKey());
      } else {
        log.warn("구매 완료 메시지 발송 실패 - 구매자: {}, 오류: {}", buyerName, response.getErrorMessage());
      }

    } catch (Exception e) {
      // 메시지 발송 실패가 구매를 막아서는 안됩니다
      log.error("구매 완료 메시지 발송 중 오류 발생 - 구매자: {}", buyerName, e);
    }
  }

  /** 3. 판매자 - 판매 알림 */
  public void sendSaleCompleteMessage(
      String phoneNumber, String buyerName, String contentTitle, BigDecimal price, Long contentId) {
    try {
      String messageContent = null;
      String title = "[Groble] 판매 알림";
      // 3) 콘텐츠 상세 URL (경로 세그먼트 안전 인코딩)
      String contentUrl =
          UriComponentsBuilder.fromHttpUrl("https://www.groble.im")
              .path("/manage/store/products/{contentId}")
              .buildAndExpand(contentId)
              .encode()
              .toUriString();

      List<ButtonInfo> buttons =
          Arrays.asList(
              ButtonInfo.builder()
                  .name("내역 확인하기")
                  .type("WL") // 웹링크
                  .urlMobile(contentUrl)
                  .urlPc(contentUrl)
                  .build());
      log.info("판매 완료 알림톡 발송 시작 - 구매자: {}, 템플릿코드: {}", buyerName, saleCompleteTemplateCode);

      // 알림톡 발송
      MessageResponse response =
          messageService.sendAlimtalk(
              phoneNumber,
              saleCompleteTemplateCode,
              title,
              messageContent,
              kakaoSenderKey,
              buttons);

      if (response.isSuccess()) {
        log.info("판매 완료 메시지 발송 성공 - 구매자: {}, 메시지키: {}", buyerName, response.getMessageKey());
      } else {
        log.warn("판매 완료 메시지 발송 실패 - 구매자: {}, 오류: {}", buyerName, response.getErrorMessage());
      }

    } catch (Exception e) {
      // 메시지 발송 실패가 판매를 막아서는 안됩니다
      log.error("판매 완료 메시지 발송 중 오류 발생 - 구매자: {}", buyerName, e);
    }
  }

  // 판매 중단 알림
  public void sendContentDiscontinuedMessage(
      String phoneNumber, String makerName, String contentTitle, Long contentId) {
    try {
      String messageContent = null;
      String title = "[Groble] 판매 중단 알림";
      // 3) 콘텐츠 상세 URL (경로 세그먼트 안전 인코딩)
      String contentUrl =
          UriComponentsBuilder.fromHttpUrl("https://www.groble.im")
              .path("/products/{contentId}")
              .buildAndExpand(contentId)
              .encode()
              .toUriString();

      List<ButtonInfo> buttons =
          Arrays.asList(
              ButtonInfo.builder()
                  .name("확인하러 가기")
                  .type("WL") // 웹링크
                  .urlMobile(contentUrl)
                  .urlPc(contentUrl)
                  .build());
      log.info("판매 중단 알림톡 발송 시작 - 메이커: {}, 템플릿코드: {}", makerName, contentDiscontinuedTemplateCode);

      // 알림톡 발송
      MessageResponse response =
          messageService.sendAlimtalk(
              phoneNumber,
              contentDiscontinuedTemplateCode,
              title,
              messageContent,
              kakaoSenderKey,
              buttons);

      if (response.isSuccess()) {
        log.info("판매 중단 메시지 발송 성공 - 메이커: {}, 메시지키: {}", makerName, response.getMessageKey());
      } else {
        log.warn("판매 중단 메시지 발송 실패 - 메이커: {}, 오류: {}", makerName, response.getErrorMessage());
      }

    } catch (Exception e) {
      // 메시지 발송 실패가 판매를 막아서는 안됩니다
      log.error("판매 중단 메시지 발송 중 오류 발생 - 메이커: {}", makerName, e);
    }
  }

  public void sendReviewRegisteredMessage(
      String phoneNumber,
      String buyerName,
      String sellerName,
      String contentTitle,
      Long contentId,
      Long reviewId) {
    try {
      String messageContent = null;
      String title = "[Groble] 리뷰 등록 알림";

      // 3) 콘텐츠 상세 URL
      String contentUrl =
          UriComponentsBuilder.fromHttpUrl("https://www.groble.im")
              .path("/manage/store/products/{contentId}/reviews/{reviewId}")
              .buildAndExpand(contentId, reviewId) // ✅ 둘 다 전달
              .encode()
              .toUriString();

      List<ButtonInfo> buttons =
          Arrays.asList(
              ButtonInfo.builder()
                  .name("확인하러 가기")
                  .type("WL") // 웹링크
                  .urlMobile(contentUrl)
                  .urlPc(contentUrl)
                  .build());
      log.info("리뷰 등록 알림톡 발송 시작 - 구매자: {}, 템플릿코드: {}", buyerName, reviewRegisteredTemplateCode);

      // 알림톡 발송
      MessageResponse response =
          messageService.sendAlimtalk(
              phoneNumber,
              reviewRegisteredTemplateCode,
              title,
              messageContent,
              kakaoSenderKey,
              buttons);

      if (response.isSuccess()) {
        log.info("리뷰 등록 메시지 발송 성공 - 구매자: {}, 메시지키: {}", buyerName, response.getMessageKey());
      } else {
        log.warn("리뷰 등록 메시지 발송 실패 - 구매자: {}, 오류: {}", buyerName, response.getErrorMessage());
      }

    } catch (Exception e) {
      // 메시지 발송 실패가 리뷰 등록을 막아서는 안됩니다
      log.error("리뷰 등록 메시지 발송 중 오류 발생 - 구매자: {}", buyerName, e);
    }
  }

  public void sendMakerCertifiedMessage(String phoneNumber, String makerName) {
    try {
      String messageContent = null;
      String title = "[Groble] 인증 완료";
      List<ButtonInfo> buttons =
          Arrays.asList(
              ButtonInfo.builder()
                  .name("마이페이지 바로가기")
                  .type("WL") // 웹링크
                  .urlMobile("https://www.groble.im/users/profile/info")
                  .urlPc("https://www.groble.im/users/profile/info")
                  .build());

      log.info(
          "메이커 인증 완료 알림톡 발송 시작 - 메이커: {}, 템플릿코드: {}", makerName, verificationCompleteTemplateCode);

      // 알림톡 발송
      MessageResponse response =
          messageService.sendAlimtalk(
              phoneNumber,
              verificationCompleteTemplateCode,
              title,
              messageContent,
              kakaoSenderKey,
              buttons);

      if (response.isSuccess()) {
        log.info("메이커 인증 완료 메시지 발송 성공 - 메이커: {}, 메시지키: {}", makerName, response.getMessageKey());
      } else {
        log.warn("메이커 인증 완료 메시지 발송 실패 - 메이커: {}, 오류: {}", makerName, response.getErrorMessage());
      }

    } catch (Exception e) {
      // 메시지 발송 실패가 인증을 막아서는 안됩니다
      log.error("메이커 인증 완료 메시지 발송 중 오류 발생 - 메이커: {}", makerName, e);
    }
  }
}
