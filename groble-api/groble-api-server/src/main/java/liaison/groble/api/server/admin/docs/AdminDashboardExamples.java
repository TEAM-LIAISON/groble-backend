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
}
