package liaison.groble.api.server.sell.docs;

public final class SellContentExamples {

  private SellContentExamples() {}

  public static final String SELL_MANAGE_PAGE_SUCCESS_EXAMPLE =
      """
      {
        "status": "SUCCESS",
        "code": 200,
        "message": "판매 관리 페이지 조회가 성공적으로 처리되었습니다.",
        "data": {
          "title": "자바 프로그래밍 코칭",
          "contentSellDetail": {
            "totalPaymentPrice": 600000,
            "totalPurchaseCustomer": 500,
            "totalReviewCount": 30
          },
          "contentSellList": [
            {
              "purchaseId": 200,
              "contentTitle": "자바 프로그래밍 코칭",
              "purchasedAt": "2025-04-20 10:15:30",
              "purchaserNickname": "뚜비뚜비",
              "purchaserEmail": "example@example.com",
              "purchaserPhoneNumber": "010-3661-4067",
              "selectedOptionName": "기본 패키지",
              "finalPrice": 29900
            },
            {
              "purchaseId": 201,
              "contentTitle": "자바 프로그래밍 코칭",
              "purchasedAt": "2025-04-19 14:32:15",
              "purchaserNickname": "개발자김씨",
              "purchaserEmail": "developer@example.com",
              "purchaserPhoneNumber": "010-1234-5678",
              "selectedOptionName": "프리미엄 패키지",
              "finalPrice": 49900
            }
          ],
          "contentReviewList": [
            {
              "reviewId": 100,
              "reviewStatus": "ACTIVE",
              "contentTitle": "자바 프로그래밍 코칭",
              "createdAt": "2025-04-20 10:15:30",
              "reviewerNickname": "뚜비뚜비",
              "reviewContent": "정말 도움이 많이 되었습니다. 강사님이 친절하게 설명해주셔서 이해하기 쉬웠어요!",
              "selectedOptionName": "기본 패키지",
              "rating": 4.5,
              "reviewReplies": [
                {
                  "replyId": 150,
                  "createdAt": "2025-04-20 11:30:45",
                  "replierNickname": "김그로블",
                  "replyContent": "좋은 리뷰 감사합니다! 앞으로도 더 나은 콘텐츠로 찾아뵙겠습니다."
                }
              ]
            },
            {
              "reviewId": 101,
              "reviewStatus": "ACTIVE",
              "contentTitle": "자바 프로그래밍 코칭",
              "createdAt": "2025-04-19 16:22:10",
              "reviewerNickname": "개발자김씨",
              "reviewContent": "실무에 바로 적용할 수 있는 내용들로 구성되어 있어서 좋았습니다.",
              "selectedOptionName": "프리미엄 패키지",
              "rating": 5.0,
              "reviewReplies": []
            }
          ]
        },
        "timestamp": "2025-08-31T07:06:06.312Z"
      }
      """;

  public static final String CONTENT_SELL_LIST_SUCCESS_EXAMPLE =
      """
      {
        "status": "SUCCESS",
        "code": 200,
        "message": "판매 콘텐츠 리스트 조회가 성공적으로 처리되었습니다.",
        "data": {
          "items": [
            {
              "purchaseId": 200,
              "contentTitle": "자바 프로그래밍 코칭",
              "purchasedAt": "2025-04-20 10:15:30",
              "purchaserNickname": "뚜비뚜비",
              "purchaserEmail": "example@example.com",
              "purchaserPhoneNumber": "010-3661-4067",
              "selectedOptionName": "기본 패키지",
              "finalPrice": 29900
            },
            {
              "purchaseId": 201,
              "contentTitle": "자바 프로그래밍 코칭",
              "purchasedAt": "2025-04-19 14:32:15",
              "purchaserNickname": "개발자김씨",
              "purchaserEmail": "developer@example.com",
              "purchaserPhoneNumber": "010-1234-5678",
              "selectedOptionName": "프리미엄 패키지",
              "finalPrice": 49900
            }
          ],
          "pageInfo": {
            "currentPage": 0,
            "totalPages": 10,
            "pageSize": 12,
            "totalElements": 120,
            "first": true,
            "last": false,
            "empty": false
          }
        },
        "timestamp": "2025-08-31T07:06:06.312Z"
      }
      """;

  public static final String CONTENT_SELL_DETAIL_SUCCESS_EXAMPLE =
      """
      {
        "status": "SUCCESS",
        "code": 200,
        "message": "판매 콘텐츠 상세 조회가 성공적으로 처리되었습니다.",
        "data": {
          "purchaseId": 200,
          "contentTitle": "자바 프로그래밍 코칭",
          "purchasedAt": "2025-04-20 10:15:30",
          "purchaserNickname": "뚜비뚜비",
          "purchaserEmail": "example@example.com",
          "purchaserPhoneNumber": "010-3661-4067",
          "selectedOptionName": "기본 패키지",
          "finalPrice": 29900
        },
        "timestamp": "2025-08-31T07:06:06.312Z"
      }
      """;

  public static final String CONTENT_REVIEW_LIST_SUCCESS_EXAMPLE =
      """
      {
        "status": "SUCCESS",
        "code": 200,
        "message": "리뷰 리스트 조회가 성공적으로 처리되었습니다.",
        "data": {
          "items": [
            {
              "reviewId": 100,
              "reviewStatus": "ACTIVE",
              "contentTitle": "자바 프로그래밍 코칭",
              "createdAt": "2025-04-20 10:15:30",
              "reviewerNickname": "뚜비뚜비",
              "reviewContent": "정말 도움이 많이 되었습니다. 강사님이 친절하게 설명해주셔서 이해하기 쉬웠어요!",
              "selectedOptionName": "기본 패키지",
              "rating": 4.5,
              "reviewReplies": [
                {
                  "replyId": 150,
                  "createdAt": "2025-04-20 11:30:45",
                  "replierNickname": "김그로블",
                  "replyContent": "좋은 리뷰 감사합니다! 앞으로도 더 나은 콘텐츠로 찾아뵙겠습니다."
                }
              ]
            },
            {
              "reviewId": 101,
              "reviewStatus": "ACTIVE",
              "contentTitle": "자바 프로그래밍 코칭",
              "createdAt": "2025-04-19 16:22:10",
              "reviewerNickname": "개발자김씨",
              "reviewContent": "실무에 바로 적용할 수 있는 내용들로 구성되어 있어서 좋았습니다.",
              "selectedOptionName": "프리미엄 패키지",
              "rating": 5.0,
              "reviewReplies": []
            }
          ],
          "pageInfo": {
            "currentPage": 0,
            "totalPages": 5,
            "pageSize": 12,
            "totalElements": 45,
            "first": true,
            "last": false,
            "empty": false
          }
        },
        "timestamp": "2025-08-31T07:06:06.312Z"
      }
      """;

  public static final String CONTENT_REVIEW_DETAIL_SUCCESS_EXAMPLE =
      """
      {
        "status": "SUCCESS",
        "code": 200,
        "message": "리뷰 상세 조회가 성공적으로 처리되었습니다.",
        "data": {
          "reviewId": 100,
          "reviewStatus": "ACTIVE",
          "contentTitle": "자바 프로그래밍 코칭",
          "createdAt": "2025-04-20 10:15:30",
          "reviewerNickname": "뚜비뚜비",
          "reviewContent": "정말 도움이 많이 되었습니다. 강사님이 친절하게 설명해주셔서 이해하기 쉬웠어요! 특히 실습 위주로 진행되어서 바로바로 적용해볼 수 있었던 점이 가장 좋았습니다.",
          "selectedOptionName": "기본 패키지",
          "rating": 4.5,
          "reviewReplies": [
            {
              "replyId": 150,
              "createdAt": "2025-04-20 11:30:45",
              "replierNickname": "김그로블",
              "replyContent": "좋은 리뷰 감사합니다! 앞으로도 더 나은 콘텐츠로 찾아뵙겠습니다."
            },
            {
              "replyId": 151,
              "createdAt": "2025-04-20 14:15:20",
              "replierNickname": "김그로블",
              "replyContent": "추가적으로 궁금한 점이 있으시면 언제든 문의주세요!"
            }
          ]
        },
        "timestamp": "2025-08-31T07:06:06.312Z"
      }
      """;
}
