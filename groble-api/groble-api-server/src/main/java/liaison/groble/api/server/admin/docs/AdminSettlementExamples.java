package liaison.groble.api.server.admin.docs;

public final class AdminSettlementExamples {

  private AdminSettlementExamples() {}

  public static final String ALL_USERS_SETTLEMENTS_SUCCESS_EXAMPLE =
      """
      {
        "success": true,
        "code": "SUCCESS",
        "message": "전체 사용자 정산 목록 조회가 성공적으로 처리되었습니다.",
        "data": {
          "items": [
            {
              "settlementId": 123,
              "scheduledSettlementDate": "2025-01-15",
              "contentType": "COACHING",
              "settlementAmount": 180000.00,
              "settlementStatus": "PENDING",
              "verificationStatus": "VERIFIED",
              "isBusinessSeller": true,
              "businessType": "INDIVIDUAL_GENERAL",
              "bankAccountOwner": "김그로블",
              "bankName": "국민은행",
              "bankAccountNumber": "123456-12-123456",
              "copyOfBankbookUrl": "https://example.com/bankbook/copy1.jpg",
              "businessLicenseFileUrl": "https://example.com/license/business1.pdf",
              "taxInvoiceEmail": "maker@example.com"
            },
            {
              "settlementId": 124,
              "scheduledSettlementDate": "2025-01-15",
              "contentType": "DOCUMENT",
              "settlementAmount": 95000.00,
              "settlementStatus": "PENDING",
              "verificationStatus": "IN_PROGRESS",
              "isBusinessSeller": false,
              "businessType": null,
              "bankAccountOwner": "이메이커",
              "bankName": "신한은행",
              "bankAccountNumber": "110-123-123456",
              "copyOfBankbookUrl": "https://example.com/bankbook/copy2.jpg",
              "businessLicenseFileUrl": null,
              "taxInvoiceEmail": "personal.maker@example.com"
            },
            {
              "settlementId": 125,
              "scheduledSettlementDate": "2025-01-10",
              "contentType": "COACHING",
              "settlementAmount": 250000.00,
              "settlementStatus": "COMPLETED",
              "verificationStatus": "VERIFIED",
              "isBusinessSeller": true,
              "businessType": "CORPORATION",
              "bankAccountOwner": "(주)그로블코칭",
              "bankName": "하나은행",
              "bankAccountNumber": "123-456789-12345",
              "copyOfBankbookUrl": "https://example.com/bankbook/copy3.jpg",
              "businessLicenseFileUrl": "https://example.com/license/business3.pdf",
              "taxInvoiceEmail": "tax@groblecoaching.com"
            }
          ],
          "pageInfo": {
            "currentPage": 0,
            "totalPages": 5,
            "pageSize": 20,
            "totalElements": 87,
            "first": true,
            "last": false,
            "empty": false
          }
        }
      }
      """;
}
