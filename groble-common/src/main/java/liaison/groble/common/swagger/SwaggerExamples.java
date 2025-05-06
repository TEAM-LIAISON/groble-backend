package liaison.groble.common.swagger;

public class SwaggerExamples {
  // 구매자 마이페이지 요약 정보 응답 예제
  public static final String BUYER_MYPAGE_SUMMARY =
      """
            {
              "status": "SUCCESS",
              "code": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "nickname": "권동민",
                "profileImageUrl": null,
                "userType": {
                  "code": "BUYER",
                  "description": "구매자"
                },
                "canSwitchToSeller": false
              },
              "timestamp": "2025-05-06 04:26:26"
            }
            """;

  // 판매자 마이페이지 요약 정보 응답 예제
  public static final String SELLER_MYPAGE_SUMMARY =
      """
            {
              "status": "SUCCESS",
              "code": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "nickname": "김판매",
                "profileImageUrl": "https://example.com/profile.jpg",
                "userType": {
                  "code": "SELLER",
                  "description": "판매자"
                },
                "verificationStatus": {
                  "code": "APPROVED",
                  "description": "승인됨"
                }
              },
              "timestamp": "2025-05-06 04:26:26"
            }
            """;
}
