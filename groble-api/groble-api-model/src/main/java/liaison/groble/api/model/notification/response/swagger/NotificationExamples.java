package liaison.groble.api.model.notification.response.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

public class NotificationExamples {

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
                    "notificationId": 1,
                    "notificationType": "CERTIFY",
                    "subNotificationType": "MAKER_CERTIFIED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "3시간 전",
                    "notificationDetails": {
                      "nickname": "seller123"
                    }
                  }
                ]
              }
            }
            """;
  }

  @Schema(description = "판매자 인증 거부 알림 예제")
  public static class MakerCertifyRejectedExample {
    public static final String EXAMPLE =
        """
            {
              "success": true,
              "message": "알림 조회 성공",
              "data": {
                "notificationItemsResponse": [
                  {
                    "notificationId": 2,
                    "notificationType": "CERTIFY",
                    "subNotificationType": "MAKER_CERTIFY_REJECTED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "2시간 전",
                    "notificationDetails": {
                      "nickname": "seller456"
                    }
                  }
                ]
              }
            }
            """;
  }

  @Schema(description = "콘텐츠 승인 알림 예제")
  public static class ContentReviewApprovedExample {
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
                    "subNotificationType": "CONTENT_REVIEW_APPROVED",
                    "notificationReadStatus": "READ",
                    "notificationOccurTime": "1일 전",
                    "notificationDetails": {
                      "contentId": 12345,
                      "thumbnailUrl": "https://example.com/thumbnail.jpg"
                    }
                  }
                ]
              }
            }
            """;
  }

  @Schema(description = "콘텐츠 심사 거부 알림 예제")
  public static class ContentReviewRejectedExample {
    public static final String EXAMPLE =
        """
            {
              "success": true,
              "message": "알림 조회 성공",
              "data": {
                "notificationItemsResponse": [
                  {
                    "notificationId": 4,
                    "notificationType": "REVIEW",
                    "subNotificationType": "CONTENT_REVIEW_REJECTED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "5시간 전",
                    "notificationDetails": {
                      "contentId": 54321,
                      "thumbnailUrl": "https://example.com/rejected-thumbnail.jpg"
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
                    "notificationId": 5,
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

  @Schema(description = "여러 알림 타입이 혼합된 예제")
  public static class MixedNotificationsExample {
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
                    "notificationOccurTime": "방금 전",
                    "notificationDetails": {
                      "nickname": "newuser123",
                      "systemTitle": "Groble에 오신 것을 환영합니다!"
                    }
                  },
                  {
                    "notificationId": 2,
                    "notificationType": "CERTIFY",
                    "subNotificationType": "MAKER_CERTIFIED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "3시간 전",
                    "notificationDetails": {
                      "nickname": "seller123"
                    }
                  },
                  {
                    "notificationId": 3,
                    "notificationType": "REVIEW",
                    "subNotificationType": "CONTENT_REVIEW_APPROVED",
                    "notificationReadStatus": "READ",
                    "notificationOccurTime": "1일 전",
                    "notificationDetails": {
                      "contentId": 12345,
                      "thumbnailUrl": "https://example.com/thumbnail.jpg"
                    }
                  },
                  {
                    "notificationId": 4,
                    "notificationType": "REVIEW",
                    "subNotificationType": "CONTENT_REVIEW_REJECTED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "2일 전",
                    "notificationDetails": {
                      "contentId": 54321,
                      "thumbnailUrl": "https://example.com/rejected-content.jpg"
                    }
                  }
                ]
              }
            }
            """;
  }
}
