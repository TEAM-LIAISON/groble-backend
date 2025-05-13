package liaison.groble.api.model.notification.response.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

public class NotificationExamples {

  @Schema(description = "판매자 인증 성공 알림 예제")
  public static class SellerVerifiedExample {
    public static final String EXAMPLE =
        """
            {
              "success": true,
              "message": "알림 조회 성공",
              "data": {
                "notificationItems": [
                  {
                    "notificationId": 1,
                    "notificationType": "SELLER",
                    "subNotificationType": "SELLER_VERIFIED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "3시간 전",
                    "notificationDetails": {
                      "nickname": "seller123",
                      "isVerified": true
                    }
                  }
                ]
              }
            }
            """;
  }

  @Schema(description = "판매자 인증 거부 알림 예제")
  public static class SellerRejectedExample {
    public static final String EXAMPLE =
        """
            {
              "success": true,
              "message": "알림 조회 성공",
              "data": {
                "notificationItems": [
                  {
                    "notificationId": 2,
                    "notificationType": "SELLER",
                    "subNotificationType": "SELLER_REJECTED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "2시간 전",
                    "notificationDetails": {
                      "nickname": "seller456",
                      "isVerified": false
                    }
                  }
                ]
              }
            }
            """;
  }

  @Schema(description = "콘텐츠 승인 알림 예제")
  public static class ContentApprovedExample {
    public static final String EXAMPLE =
        """
            {
              "success": true,
              "message": "알림 조회 성공",
              "data": {
                "notificationItems": [
                  {
                    "notificationId": 3,
                    "notificationType": "CONTENT",
                    "subNotificationType": "CONTENT_APPROVED",
                    "notificationReadStatus": "READ",
                    "notificationOccurTime": "1일 전",
                    "notificationDetails": {
                      "contentId": 12345,
                      "thumbnailUrl": "https://example.com/thumbnail.jpg",
                      "isContentApproved": true
                    }
                  }
                ]
              }
            }
            """;
  }

  @Schema(description = "콘텐츠 거부 알림 예제")
  public static class ContentRejectedExample {
    public static final String EXAMPLE =
        """
            {
              "success": true,
              "message": "알림 조회 성공",
              "data": {
                "notificationItems": [
                  {
                    "notificationId": 4,
                    "notificationType": "CONTENT",
                    "subNotificationType": "CONTENT_REJECTED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "5시간 전",
                    "notificationDetails": {
                      "contentId": 54321,
                      "thumbnailUrl": "https://example.com/rejected-thumbnail.jpg",
                      "isContentApproved": false
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
                "notificationItems": [
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
                "notificationItems": [
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
                    "notificationType": "SELLER",
                    "subNotificationType": "SELLER_VERIFIED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "3시간 전",
                    "notificationDetails": {
                      "nickname": "seller123",
                      "isVerified": true
                    }
                  },
                  {
                    "notificationId": 3,
                    "notificationType": "CONTENT",
                    "subNotificationType": "CONTENT_APPROVED",
                    "notificationReadStatus": "READ",
                    "notificationOccurTime": "1일 전",
                    "notificationDetails": {
                      "contentId": 12345,
                      "thumbnailUrl": "https://example.com/thumbnail.jpg",
                      "isContentApproved": true
                    }
                  },
                  {
                    "notificationId": 4,
                    "notificationType": "CONTENT",
                    "subNotificationType": "CONTENT_REJECTED",
                    "notificationReadStatus": "UNREAD",
                    "notificationOccurTime": "2일 전",
                    "notificationDetails": {
                      "contentId": 54321,
                      "thumbnailUrl": "https://example.com/rejected-content.jpg",
                      "isContentApproved": false
                    }
                  }
                ]
              }
            }
            """;
  }
}
