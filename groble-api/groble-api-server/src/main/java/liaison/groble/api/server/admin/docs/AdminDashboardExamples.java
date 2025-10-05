package liaison.groble.api.server.admin.docs;

public final class AdminDashboardExamples {

  private AdminDashboardExamples() {}

  public static final String ADMIN_DASHBOARD_OVERVIEW_SUCCESS_EXAMPLE =
      """
      {
        "status": "SUCCESS",
        "code": 200,
        "message": "관리자 대시보드 개요 조회가 성공적으로 처리되었습니다.",
        "data": {
          "totalTransactionAmount": 1500000000,
          "totalTransactionCount": 1234,
          "monthlyTransactionAmount": 50000000,
          "monthlyTransactionCount": 89,
          "userCount": 5678,
          "guestUserCount": 234,
          "totalContentCount": 890,
          "activeContentCount": 567,
          "documentTypeCount": 234,
          "coachingTypeCount": 123,
          "membershipTypeCount": 45
        },
        "timestamp": "2025-08-31T07:06:06.312Z"
      }
      """;

  public static final String ADMIN_DASHBOARD_TREND_SUCCESS_EXAMPLE =
      """
      {
        "status": "SUCCESS",
        "code": 200,
        "message": "관리자 대시보드 추세 데이터 조회에 성공하였습니다.",
        "data": {
          "startDate": "2025-01-01",
          "endDate": "2025-01-07",
          "totalRevenue": 3200000,
          "totalSalesCount": 128,
          "averageOrderValue": 25000,
          "daily": [
            {
              "date": "2025-01-01",
              "totalRevenue": 450000,
              "totalSalesCount": 18,
              "averageOrderValue": 25000
            },
            {
              "date": "2025-01-02",
              "totalRevenue": 520000,
              "totalSalesCount": 20,
              "averageOrderValue": 26000
            }
          ]
        },
        "timestamp": "2025-08-31T07:06:06.312Z"
      }
      """;

  public static final String ADMIN_DASHBOARD_TOP_CONTENTS_SUCCESS_EXAMPLE =
      """
      {
        "status": "SUCCESS",
        "code": 200,
        "message": "관리자 대시보드 인기 콘텐츠 조회에 성공하였습니다.",
        "data": {
          "startDate": "2025-01-01",
          "endDate": "2025-01-31",
          "limit": 5,
          "contents": [
            {
              "contentId": 101,
              "contentTitle": "비즈니스 코칭 패키지",
              "sellerId": 15,
              "sellerNickname": "growth_maker",
              "totalRevenue": 980000,
              "totalSalesCount": 28,
              "averageOrderValue": 35000
            },
            {
              "contentId": 77,
              "contentTitle": "스타트업 자금조달 문서",
              "sellerId": 8,
              "sellerNickname": "deal_closer",
              "totalRevenue": 750000,
              "totalSalesCount": 25,
              "averageOrderValue": 30000
            }
          ]
        },
        "timestamp": "2025-08-31T07:06:06.312Z"
      }
      """;
}
