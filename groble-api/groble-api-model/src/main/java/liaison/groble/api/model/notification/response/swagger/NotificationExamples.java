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
                        "notificationOccurTime": "방금 전",
                        "notificationDetails": {
                          "contentId": 1001,
                          "reviewId": null
                        }
                      }
                    ]
                  }
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
                        "notificationOccurTime": "10분 전",
                        "notificationDetails": {
                          "contentId": 1001,
                          "reviewId": 2002
                        }
                      }
                    ]
                  }
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
                        "notificationOccurTime": "1시간 전",
                        "notificationDetails": {
                          "contentId": 1001,
                          "reviewId": 3003,
                          "thumbnailUrl": "https://example.com/review-thumb.jpg"
                        }
                      }
                    ]
                  }
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
                        "notificationOccurTime": "5분 전",
                        "notificationDetails": {
                          "contentId": 4004
                        }
                      }
                    ]
                  }
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
                        "notificationOccurTime": "3시간 전",
                        "notificationDetails": {
                          "contentId": 4004
                        }
                      }
                    ]
                  }
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
                        "notificationId": 6,
                        "notificationType": "CERTIFY",
                        "subNotificationType": "MAKER_CERTIFIED",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "2시간 전",
                        "notificationDetails": {
                          "nickname": "seller123"
                        }
                      }
                    ]
                  }
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
                        "notificationId": 7,
                        "notificationType": "CERTIFY",
                        "subNotificationType": "MAKER_CERTIFY_REJECTED",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "1시간 전",
                        "notificationDetails": {
                          "nickname": "seller456"
                        }
                      }
                    ]
                  }
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
                        "notificationId": 8,
                        "notificationType": "SYSTEM",
                        "subNotificationType": "WELCOME_GROBLE",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "방금 전",
                        "notificationDetails": {
                          "nickname": "newuser123",
                          "systemTitle": "Groble에 오신 것을 환영합니다!"
                        }
                      }
                    ]
                  }
                }
                """;
  }

  @Schema(description = "혼합 알림 예제")
  public static class MixedNotificationsExample {
    public static final String EXAMPLE =
        """
                {
                  "success": true,
                  "message": "알림 조회 성공",
                  "data": {
                    "notificationItemsResponse": [
                      /* PURCHASE: 구매 */
                      {
                        "notificationId": 1,
                        "notificationType": "PURCHASE",
                        "subNotificationType": "CONTENT_PURCHASED",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "방금 전",
                        "notificationDetails": { "contentId": 1001, "reviewId": null }
                      },
                      /* PURCHASE: 리뷰 답글 */
                      {
                        "notificationId": 2,
                        "notificationType": "PURCHASE",
                        "subNotificationType": "CONTENT_REVIEW_REPLY",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "10분 전",
                        "notificationDetails": { "contentId": 1001, "reviewId": 2002 }
                      },
                      /* REVIEW: 리뷰 등록 */
                      {
                        "notificationId": 3,
                        "notificationType": "REVIEW",
                        "subNotificationType": "CONTENT_REVIEWED",
                        "notificationReadStatus": "READ",
                        "notificationOccurTime": "1시간 전",
                        "notificationDetails": {
                          "contentId": 1001,
                          "reviewId": 3003,
                          "thumbnailUrl": "https://example.com/review-thumb.jpg"
                        }
                      },
                      /* SELL: 판매 */
                      {
                        "notificationId": 4,
                        "notificationType": "SELL",
                        "subNotificationType": "CONTENT_SOLD",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "5분 전",
                        "notificationDetails": { "contentId": 4004 }
                      },
                      /* SELL: 판매 중단 */
                      {
                        "notificationId": 5,
                        "notificationType": "SELL",
                        "subNotificationType": "CONTENT_SOLD_STOPPED",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "3시간 전",
                        "notificationDetails": { "contentId": 4004 }
                      },
                      /* CERTIFY: 인증 성공 */
                      {
                        "notificationId": 6,
                        "notificationType": "CERTIFY",
                        "subNotificationType": "MAKER_CERTIFIED",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "2시간 전",
                        "notificationDetails": { "nickname": "seller123" }
                      },
                      /* CERTIFY: 인증 거부 */
                      {
                        "notificationId": 7,
                        "notificationType": "CERTIFY",
                        "subNotificationType": "MAKER_CERTIFY_REJECTED",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "1시간 전",
                        "notificationDetails": { "nickname": "seller456" }
                      },
                      /* SYSTEM: 환영 */
                      {
                        "notificationId": 8,
                        "notificationType": "SYSTEM",
                        "subNotificationType": "WELCOME_GROBLE",
                        "notificationReadStatus": "UNREAD",
                        "notificationOccurTime": "방금 전",
                        "notificationDetails": {
                          "nickname": "newuser123",
                          "systemTitle": "Groble에 오신 것을 환영합니다!"
                        }
                      }
                    ]
                  }
                }
                """;
  }
}
