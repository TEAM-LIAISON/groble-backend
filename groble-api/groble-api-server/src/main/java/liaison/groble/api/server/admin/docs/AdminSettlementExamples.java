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

  public static final String ADMIN_SETTLEMENT_DETAIL_SUCCESS_EXAMPLE =
      """
      {
        "success": true,
        "code": "SUCCESS",
        "message": "정산 상세 정보 조회가 성공적으로 처리되었습니다.",
        "data": {
          "settlementId": 123,
          "settlementStartDate": "2025-01-01",
          "settlementEndDate": "2025-01-31",
          "scheduledSettlementDate": "2025-02-15",
          "totalSalesAmount": 2150000.00,
          "totalRefundAmount": 150000.00,
          "refundCount": 2,
          "totalFee": 45600.00,
          "totalFeeDisplay": 48000.00,
          "settlementAmount": 180000.00,
          "settlementAmountDisplay": 192000.00,
          "pgFee": 3060.00,
          "pgFeeDisplay": 3240.00,
          "platformFee": 2700.00,
          "platformFeeDisplay": 2970.00,
          "pgFeeRefundExpected": 1320.00,
          "platformFeeForgone": 540.00,
          "vatAmount": 270.00,
          "feeVatDisplay": 300.00,
          "platformFeeRate": 0.0150,
          "platformFeeRateDisplay": 0.0165,
          "platformFeeRateBaseline": 0.0180,
          "pgFeeRate": 0.0170,
          "pgFeeRateDisplay": 0.0180,
          "pgFeeRateBaseline": 0.0195,
          "vatRate": 0.1000,
          "settlementNote": "Payple SUCCESS - code: A0000, message: 승인완료"
        }
      }
      """;

  public static final String ADMIN_SETTLEMENT_SALES_LIST_SUCCESS_EXAMPLE =
      """
      {
        "success": true,
        "code": "SUCCESS",
        "message": "정산 판매 내역 조회가 성공적으로 처리되었습니다.",
        "data": {
          "items": [
            {
              "contentTitle": "AI 툴 개발 가이드",
              "settlementAmount": 150000.00,
              "orderStatus": "PAID",
              "purchasedAt": "2025-01-10 14:30:00"
            },
            {
              "contentTitle": "웹 개발 마스터 코스",
              "settlementAmount": 95000.00,
              "orderStatus": "PAID",
              "purchasedAt": "2025-01-08 09:15:00"
            },
            {
              "contentTitle": "디자인 패턴 완벽 가이드",
              "settlementAmount": 120000.00,
              "orderStatus": "CANCELLED",
              "purchasedAt": "2025-01-05 16:45:00"
            }
          ],
          "pageInfo": {
            "currentPage": 0,
            "totalPages": 3,
            "pageSize": 20,
            "totalElements": 52,
            "first": true,
            "last": false,
            "empty": false
          }
        }
      }
      """;

  public static final String PG_FEE_ADJUSTMENTS_SUCCESS_EXAMPLE =
      """
      {
        "success": true,
        "code": "SUCCESS",
        "message": "PG 수수료 차액 내역 조회가 성공적으로 처리되었습니다.",
        "data": {
          "items": [
            {
              "settlementId": 321,
              "settlementItemId": 654,
              "purchaseId": 987,
              "sellerId": 42,
              "sellerNickname": "groble_maker",
              "merchantUid": "ORD-20250201-00001",
              "contentTitle": "AI 툴 개발 가이드",
              "salesAmount": 150000.00,
              "pgFeeApplied": 4350.00,
              "pgFeeDisplay": 2550.00,
              "pgFeeDifference": 1800.00,
              "feeVat": 435.00,
              "feeVatDisplay": 255.00,
              "feeVatDifference": 180.00,
              "pgFeeRefundExpected": 1980.00,
              "totalFee": 4785.00,
              "totalFeeDisplay": 2805.00,
              "settlementAmount": 145215.00,
              "settlementAmountDisplay": 147195.00,
              "purchasedAt": "2025-02-01 12:34:56",
              "orderStatus": "PAID",
              "capturedPgFeeRate": 0.0290,
              "capturedPgFeeRateDisplay": 0.0170,
              "capturedPgFeeRateBaseline": 0.0170,
              "capturedVatRate": 0.1000
            }
          ],
          "pageInfo": {
            "currentPage": 0,
            "totalPages": 1,
            "pageSize": 20,
            "totalElements": 1,
            "first": true,
            "last": true,
            "empty": false
          }
        }
      }
      """;
}
