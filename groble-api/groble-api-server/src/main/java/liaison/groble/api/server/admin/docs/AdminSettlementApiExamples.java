package liaison.groble.api.server.admin.docs;

public final class AdminSettlementApiExamples {

  private AdminSettlementApiExamples() {}

  public static final String SETTLEMENT_APPROVAL_SUCCESS_EXAMPLE =
      """
      {
        "success": true,
        "code": 200,
        "message": "정산 승인 요청이 성공적으로 처리되었습니다.",
        "data": {
          "success": true,
          "approvedSettlementCount": 2,
          "approvedItemCount": 3,
          "totalApprovedAmount": 285000.00,
          "approvedAt": "2025-09-09T15:30:45",
          "paypleResult": {
            "success": true,
            "responseCode": "SUCCESS",
            "responseMessage": "페이플 정산 승인 및 실행 완료",
            "accessToken": "AUTH_TOKEN_12345",
            "expiresIn": "3600"
          },
          "failedSettlements": null,
          "excludedRefundedItemCount": 1
        },
        "timestamp": "2025-09-09 15:30:45"
      }
      """;

  public static final String SETTLEMENT_APPROVAL_PARTIAL_SUCCESS_EXAMPLE =
      """
      {
        "success": false,
        "code": 200,
        "message": "정산 승인 요청이 성공적으로 처리되었습니다.",
        "data": {
          "success": false,
          "approvedSettlementCount": 1,
          "approvedItemCount": 1,
          "totalApprovedAmount": 120000.00,
          "approvedAt": "2025-09-09T15:30:45",
          "paypleResult": {
            "success": true,
            "responseCode": "SUCCESS",
            "responseMessage": "페이플 정산 승인 및 실행 완료",
            "accessToken": "AUTH_TOKEN_12345",
            "expiresIn": "3600"
          },
          "failedSettlements": [
            {
              "settlementId": 156,
              "failureReason": "이미 완료된 정산입니다"
            }
          ],
          "excludedRefundedItemCount": 0
        },
        "timestamp": "2025-09-09 15:30:45"
      }
      """;

  public static final String SETTLEMENT_APPROVAL_PAYPLE_FAILURE_EXAMPLE =
      """
      {
        "success": false,
        "code": 200,
        "message": "정산 승인 요청이 성공적으로 처리되었습니다.",
        "data": {
          "success": false,
          "approvedSettlementCount": 0,
          "approvedItemCount": 0,
          "totalApprovedAmount": 0.00,
          "approvedAt": "2025-09-09T15:30:45",
          "paypleResult": {
            "success": false,
            "responseCode": "ERROR",
            "responseMessage": "페이플 정산 승인 및 실행 실패: 페이플 파트너 인증 실패",
            "accessToken": null,
            "expiresIn": null
          },
          "failedSettlements": [
            {
              "settlementId": 155,
              "failureReason": "페이플 정산 실행 실패"
            }
          ],
          "excludedRefundedItemCount": 0
        },
        "timestamp": "2025-09-09 15:30:45"
      }
      """;
}
