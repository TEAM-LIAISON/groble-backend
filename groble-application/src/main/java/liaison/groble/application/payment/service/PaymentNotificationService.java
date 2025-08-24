package liaison.groble.application.payment.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.payment.event.FreePaymentCompletedEvent;
import liaison.groble.application.payment.event.PaymentCompletedEvent;
import liaison.groble.application.payment.event.PaymentRefundedEvent;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.port.EmailSenderPort;
import liaison.groble.domain.user.entity.User;
import liaison.groble.external.discord.dto.payment.ContentPaymentSuccessReportDTO;
import liaison.groble.external.discord.service.payment.ContentPaymentSuccessReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationService {
  private final NotificationService notificationService;
  private final EmailSenderPort emailSenderPort;
  private final UserReader userReader;
  private final ContentReader contentReader;
  private final ContentPaymentSuccessReportService contentPaymentSuccessReportService;

  @Async("defaultAsyncExecutor") // 명시적으로 Executor 지정
  public void processAsyncPaymentCompletedEvent(PaymentCompletedEvent event) {
    log.info(
        "비동기 결제 완료 처리 시작 - orderId: {}, 쓰레드: {}",
        event.getOrderId(),
        Thread.currentThread().getName());

    try {
      // 1. 판매자에게 알림 발송
      sendSellerNotification(event);
      sendSellerATNotification(event);
      // 2. 구매자에게 알림 발송
      sendBuyerNotification(event);
      sendBuyerATNotification(event);
      // 3. 판매자에게 이메일 발송
      sendSaleNotificationEmail(event);

      // 4. 결제 완료 디스코드 알림 발송
      sendDiscordPaymentSuccessNotification(event);
      log.info("비동기 결제 완료 처리 완료 - orderId: {}", event.getOrderId());

    } catch (Exception e) {
      log.error("비동기 결제 완료 처리 실패 - orderId: {}", event.getOrderId(), e);
      // 필요시 재처리 로직이나 알람 발송
    }
  }

  @Async("defaultAsyncExecutor") // 명시적으로 Executor 지정
  public void processAsyncFreePaymentCompletedEvent(FreePaymentCompletedEvent event) {
    log.info(
        "비동기 무료 결제 완료 처리 시작 - orderId: {}, 쓰레드: {}",
        event.getOrderId(),
        Thread.currentThread().getName());

    try {
      // 1. 판매자에게 알림 발송
      sendSellerFreePayNotification(event);

      // 2. 구매자에게 알림 발송
      sendBuyerFreePayNotification(event);

      // 3. 판매자에게 이메일 발송
      sendSaleFreePayNotificationEmail(event);

      // 4. 결제 완료 디스코드 알림 발송
      sendDiscordFreePaymentSuccessNotification(event);
      log.info("비동기 무료 결제 완료 처리 완료 - orderId: {}", event.getOrderId());

    } catch (Exception e) {
      log.error("비동기 무료 결제 완료 처리 실패 - orderId: {}", event.getOrderId(), e);
      // 필요시 재처리 로직이나 알람 발송
    }
  }

  @Async("defaultAsyncExecutor") // 명시적으로 Executor 지정
  public void processAsyncPaymentRefundedEvent(PaymentRefundedEvent event) {
    log.info(
        "비동기 환불 처리 시작 - orderId: {}, 쓰레드: {}",
        event.getOrderId(),
        Thread.currentThread().getName());
    try {
      // 1. 구매자에게 환불 알림 발송
      sendRefundNotification(event);

      // 2. 구매자에게 환불 이메일 발송
      sendRefundEmail(event);

      log.info("환불 완료 이벤트 처리 완료 - orderId: {}", event.getOrderId());

    } catch (Exception e) {
      log.error("환불 완료 이벤트 처리 중 오류 발생 - orderId: {}", event.getOrderId(), e);
    }
  }

  /** 구매자 알림 발송 */
  private void sendBuyerNotification(PaymentCompletedEvent event) {
    try {
      User buyer = userReader.getUserById(event.getUserId());
      Content content = contentReader.getContentById(event.getContentId());
      notificationService.sendContentPurchasedNotification(
          buyer, event.getContentId(), event.getMerchantUid(), content.getThumbnailUrl());
      log.debug(
          "구매자 알림 발송 완료 - buyerId: {}, contentId: {}", event.getUserId(), event.getContentId());
    } catch (Exception e) {
      log.error("구매자 알림 발송 실패 - buyerId: {}", event.getUserId(), e);
    }
  }

  private void sendBuyerATNotification(PaymentCompletedEvent event) {
    try {
      User buyer = userReader.getUserById(event.getUserId());
      notificationService.sendPurchaseCompleteMessage(
          buyer.getPhoneNumber(),
          buyer.getNickname(),
          event.getContentTitle(),
          event.getAmount(),
          event.getMerchantUid());
    } catch (Exception e) {
      log.error("구매자 알림 발송 실패 - buyerId: {}", event.getUserId(), e);
    }
  }

  /** 구매자 무료 결제 알림 발송 */
  private void sendBuyerFreePayNotification(FreePaymentCompletedEvent event) {
    try {
      User buyer = userReader.getUserById(event.getUserId());
      Content content = contentReader.getContentById(event.getContentId());
      notificationService.sendContentPurchasedNotification(
          buyer, event.getContentId(), event.getMerchantUid(), content.getThumbnailUrl());
      log.debug(
          "구매자 무료 결제 알림 발송 완료 - buyerId: {}, contentId: {}",
          event.getUserId(),
          event.getContentId());
    } catch (Exception e) {
      log.error("구매자 무료 결제 알림 발송 실패 - buyerId: {}", event.getUserId(), e);
    }
  }

  /** 판매자 알림 발송 */
  private void sendSellerNotification(PaymentCompletedEvent event) {
    try {
      User seller = userReader.getUserById(event.getSellerId());
      Content content = contentReader.getContentById(event.getContentId());
      notificationService.sendContentSoldNotification(
          seller, event.getContentId(), event.getPurchaseId(), content.getThumbnailUrl());
      log.debug(
          "판매자 알림 발송 완료 - sellerId: {}, contentId: {}", event.getSellerId(), event.getContentId());
    } catch (Exception e) {
      log.error("판매자 알림 발송 실패 - sellerId: {}", event.getSellerId(), e);
    }
  }

  private void sendSellerATNotification(PaymentCompletedEvent event) {
    try {
      User seller = userReader.getUserById(event.getSellerId());
      User buyer = userReader.getUserById(event.getUserId());
      notificationService.sendSaleCompleteMessage(
          seller.getPhoneNumber(),
          buyer.getNickname(),
          event.getContentTitle(),
          event.getAmount(),
          event.getContentId());
    } catch (Exception e) {
      log.error("판매자 알림 발송 실패 - sellerId: {}", event.getSellerId(), e);
    }
  }

  /** 판매자 무료 결제 알림 발송 */
  private void sendSellerFreePayNotification(FreePaymentCompletedEvent event) {
    try {
      User seller = userReader.getUserById(event.getSellerId());
      Content content = contentReader.getContentById(event.getContentId());
      notificationService.sendContentSoldNotification(
          seller, event.getContentId(), event.getPurchaseId(), content.getThumbnailUrl());
      log.debug(
          "판매자 무료 결제 알림 발송 완료 - sellerId: {}, contentId: {}",
          event.getSellerId(),
          event.getContentId());
    } catch (Exception e) {
      log.error("판매자 무료 결제 알림 발송 실패 - sellerId: {}", event.getSellerId(), e);
    }
  }

  /** 판매 알림 이메일 발송 */
  private void sendSaleNotificationEmail(PaymentCompletedEvent event) {
    try {
      emailSenderPort.sendSaleNotificationEmail(
          event.getSellerEmail(),
          event.getContentTitle(),
          event.getAmount(),
          event.getCompletedAt(),
          event.getContentId());
      log.debug("판매 알림 이메일 발송 완료 - orderId: {}", event.getOrderId());
    } catch (Exception e) {
      log.error("판매 알림 이메일 발송 실패 - orderId: {}", event.getOrderId(), e);
    }
  }

  /** 판매 알림 이메일 발송 */
  private void sendSaleFreePayNotificationEmail(FreePaymentCompletedEvent event) {
    try {
      emailSenderPort.sendSaleNotificationEmail(
          event.getSellerEmail(),
          event.getContentTitle(),
          event.getAmount(),
          event.getCompletedAt(),
          event.getContentId());
      log.debug("무료 콘텐츠 판매 알림 이메일 발송 완료 - orderId: {}", event.getOrderId());
    } catch (Exception e) {
      log.error("무료 콘텐츠 판매 알림 이메일 발송 실패 - orderId: {}", event.getOrderId(), e);
    }
  }

  /** 디스코드 결제 성사 알림 발송 */
  private void sendDiscordPaymentSuccessNotification(PaymentCompletedEvent event) {
    try {

      ContentPaymentSuccessReportDTO contentPaymentSuccessReportDTO =
          ContentPaymentSuccessReportDTO.builder()
              .userId(event.getUserId())
              .nickname(event.getNickname())
              .contentId(event.getContentId())
              .contentTitle(event.getContentTitle())
              .contentType(event.getContentType())
              .optionId(event.getOptionId())
              .selectedOptionName(event.getSelectedOptionName())
              .merchantUid(event.getMerchantUid())
              .purchasedAt(event.getPurchasedAt())
              .build();

      contentPaymentSuccessReportService.sendContentPaymentSuccessReport(
          contentPaymentSuccessReportDTO);

      log.debug("디스코드 결제 성사 알림 발송");
    } catch (Exception e) {
      log.error("디스코드 결제 성사 알림 발송 실패", e);
    }
  }

  /** 디스코드 결제 성사 알림 발송 */
  private void sendDiscordFreePaymentSuccessNotification(FreePaymentCompletedEvent event) {
    try {

      ContentPaymentSuccessReportDTO contentPaymentSuccessReportDTO =
          ContentPaymentSuccessReportDTO.builder()
              .userId(event.getUserId())
              .nickname(event.getNickname())
              .contentId(event.getContentId())
              .contentTitle(event.getContentTitle())
              .contentType(event.getContentType())
              .optionId(event.getOptionId())
              .selectedOptionName(event.getSelectedOptionName())
              .merchantUid(event.getMerchantUid())
              .purchasedAt(event.getPurchasedAt())
              .build();

      contentPaymentSuccessReportService.sendContentPaymentSuccessReport(
          contentPaymentSuccessReportDTO);

      log.debug("디스코드 무료 결제 성사 알림 발송");
    } catch (Exception e) {
      log.error("디스코드 무료 결제 성사 알림 발송 실패", e);
    }
  }

  /** 환불 알림 발송 */
  private void sendRefundNotification(PaymentRefundedEvent event) {
    try {
      User user = userReader.getUserById(event.getUserId());
      // 환불 알림 발송 로직
      log.debug("환불 알림 발송 완료 - userId: {}, orderId: {}", event.getUserId(), event.getOrderId());
    } catch (Exception e) {
      log.error("환불 알림 발송 실패 - userId: {}", event.getUserId(), e);
    }
  }

  /** 환불 이메일 발송 */
  private void sendRefundEmail(PaymentRefundedEvent event) {
    try {
      // 환불 이메일 발송 로직
      log.debug("환불 이메일 발송 완료 - orderId: {}", event.getOrderId());
    } catch (Exception e) {
      log.error("환불 이메일 발송 실패 - orderId: {}", event.getOrderId(), e);
    }
  }
}
