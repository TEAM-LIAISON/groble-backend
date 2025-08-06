package liaison.groble.api.model.notification.response.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

public class NotificationExamples {

  @Schema(description = "콘텐츠 구매 알림 예제")
  public static class ContentPurchasedExample {
    public static final String EXAMPLE =
        """
        {
          "success": true,
          "message": "알림 조회 성공",
          "data": {
            "notificationItemsResponse": [
              {
                "notificationId": 1,
                "notificationType": "PURCHASE",
                "subNotificationType": "CONTENT_PURCHASED",
                "notificationReadStatus": "UNREAD",
                "notificationOccurTime": "2025-08-06 15:13:16",
                "notificationDetails": {
                  "nickname": null,
                  "contentId": 1,
                  "reviewId": null,
                  "merchantUid": "2025080412345678",
                  "purchaseId": null,
                  "thumbnailUrl": "https://example.com/review-thumb.jpg",
                  "systemTitle": null
                }
              }
            ]
          },
          "timestamp": "2025-08-06 14:16:36"
        }
        """;
  }

  @Schema(description = "콘텐츠 리뷰 답글 알림 예제")
  public static class ContentReviewReplyExample {
    public static final String EXAMPLE =
        """
        {
          "success": true,
          "message": "알림 조회 성공",
          "data": {
            "notificationItemsResponse": [
              {
                "notificationId": 2,
                "notificationType": "PURCHASE",
                "subNotificationType": "CONTENT_REVIEW_REPLY",
                "notificationReadStatus": "UNREAD",
                "notificationOccurTime": "2025-08-06 15:13:16",
                "notificationDetails": {
                  "nickname": null,
                  "contentId": 1,
                  "reviewId": 200,
                  "merchantUid": null,
                  "purchaseId": null,
                  "thumbnailUrl": "https://example.com/review-thumb.jpg",
                  "systemTitle": null
                }
              }
            ]
          },
          "timestamp": "2025-08-06 14:16:36"
        }
        """;
  }

  @Schema(description = "리뷰 등록 알림 예제")
  public static class ContentReviewedExample {
    public static final String EXAMPLE =
        """
        {
          "success": true,
          "message": "알림 조회 성공",
          "data": {
            "notificationItemsResponse": [
              {
                "notificationId": 3,
                "notificationType": "REVIEW",
                "subNotificationType": "CONTENT_REVIEWED",
                "notificationReadStatus": "READ",
                "notificationOccurTime": "2025-08-06 15:13:16",
                "notificationDetails": {
                  "nickname": null,
                  "contentId": 1,
                  "reviewId": 200,
                  "merchantUid": null,
                  "purchaseId": null,
                  "thumbnailUrl": "https://example.com/review-thumb.jpg",
                  "systemTitle": null
                }
              }
            ]
          },
          "timestamp": "2025-08-06 14:16:36"
        }
        """;
  }

  @Schema(description = "상품 판매 알림 예제")
  public static class ContentSoldExample {
    public static final String EXAMPLE =
        """
        {
          "success": true,
          "message": "알림 조회 성공",
          "data": {
            "notificationItemsResponse": [
              {
                "notificationId": 4,
                "notificationType": "SELL",
                "subNotificationType": "CONTENT_SOLD",
                "notificationReadStatus": "UNREAD",
                "notificationOccurTime": "2025-08-06 15:13:16",
                "notificationDetails": {
                  "nickname": null,
                  "contentId": 1,
                  "reviewId": null,
                  "merchantUid": null,
                  "purchaseId": 100,
                  "thumbnailUrl": "https://example.com/review-thumb.jpg",
                  "systemTitle": null
                }
              }
            ]
          },
          "timestamp": "2025-08-06 14:16:36"
        }
        """;
  }

  @Schema(description = "상품 판매 중단 알림 예제")
  public static class ContentSoldStoppedExample {
    public static final String EXAMPLE =
        """
        {
          "success": true,
          "message": "알림 조회 성공",
          "data": {
            "notificationItemsResponse": [
              {
                "notificationId": 5,
                "notificationType": "SELL",
                "subNotificationType": "CONTENT_SOLD_STOPPED",
                "notificationReadStatus": "UNREAD",
                "notificationOccurTime": "2025-08-06 15:13:16",
                "notificationDetails": {
                  "nickname": null,
                  "contentId": 1,
                  "reviewId": null,
                  "merchantUid": null,
                  "purchaseId": null,
                  "thumbnailUrl": "https://example.com/review-thumb.jpg",
                  "systemTitle": null
                }
              }
            ]
          },
          "timestamp": "2025-08-06 14:16:36"
        }
        """;
  }

  @Schema(description = "메이커 인증 성공 알림 예제")
  public static class MakerCertifiedExample {
    public static final String EXAMPLE =
        """
        {
          "success": true,
          "message": "알림 조회 성공",
          "data": {
            "notificationItemsResponse": [
              {
                "notificationId": 56,
                "notificationType": "CERTIFY",
                "subNotificationType": "MAKER_CERTIFIED",
                "notificationReadStatus": "UNREAD",
                "notificationOccurTime": "2025-08-06 15:13:16",
                "notificationDetails": {
                  "nickname": "네이버메이커동민",
                  "contentId": null,
                  "reviewId": null,
                  "merchantUid": null,
                  "purchaseId": null,
                  "thumbnailUrl": null,
                  "systemTitle": null
                }
              }
            ]
          },
          "timestamp": "2025-08-06 14:16:36"
        }
        """;
  }

  @Schema(description = "메이커 인증 거부 알림 예제")
  public static class MakerCertifyRejectedExample {
    public static final String EXAMPLE =
        """
        {
          "success": true,
          "message": "알림 조회 성공",
          "data": {
            "notificationItemsResponse": [
              {
                "notificationId": 1,
                "notificationType": "CERTIFY",
                "subNotificationType": "MAKER_CERTIFY_REJECTED",
                "notificationReadStatus": "UNREAD",
                "notificationOccurTime": "2025-07-29 00:13:27",
                "notificationDetails": {
                  "nickname": "네이버메이커동민",
                  "contentId": null,
                  "reviewId": null,
                  "merchantUid": null,
                  "purchaseId": null,
                  "thumbnailUrl": null,
                  "systemTitle": null
                }
              }
            ]
          },
          "timestamp": "2025-08-06 14:16:36"
        }
        """;
  }

  @Schema(description = "시스템 환영 알림 예제")
  public static class WelcomeGrobleExample {
    public static final String EXAMPLE =
        """
        {
          "success": true,
          "message": "알림 조회 성공",
          "data": {
            "notificationItemsResponse": [
              {
                "notificationId": 1,
                "notificationType": "SYSTEM",
                "subNotificationType": "WELCOME_GROBLE",
                "notificationReadStatus": "UNREAD",
                "notificationOccurTime": "2025-07-29 00:13:27",
                "notificationDetails": {
                  "nickname": "그로블 관리자 계정",
                  "contentId": null,
                  "reviewId": null,
                  "merchantUid": null,
                  "purchaseId": null,
                  "thumbnailUrl": null,
                  "systemTitle": "그로블에 오신 것을 환영합니다!"
                }
              }
            ]
          },
          "timestamp": "2025-08-06 14:16:36"
        }
        """;
  }
}
