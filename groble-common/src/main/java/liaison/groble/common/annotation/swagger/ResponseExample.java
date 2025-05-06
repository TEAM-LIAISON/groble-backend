package liaison.groble.common.annotation.swagger;

public class ResponseExample {
  // 구매자 마이페이지 요약 정보 예제
  public static final String BUYER_SUMMARY =
      "{\n"
          + "  \"nickname\": \"권동민\",\n"
          + "  \"profileImageUrl\": null,\n"
          + "  \"userType\": {\n"
          + "    \"code\": \"BUYER\",\n"
          + "    \"description\": \"구매자\"\n"
          + "  },\n"
          + "  \"canSwitchToSeller\": false\n"
          + "}\n";

  // 판매자 마이페이지 요약 정보 예제
  public static final String SELLER_SUMMARY =
      "{\n"
          + "  \"nickname\": \"김판매\",\n"
          + "  \"profileImageUrl\": \"https://example.com/profile.jpg\",\n"
          + "  \"userType\": {\n"
          + "    \"code\": \"SELLER\",\n"
          + "    \"description\": \"판매자\"\n"
          + "  },\n"
          + "  \"verificationStatus\": {\n"
          + "    \"code\": \"APPROVED\",\n"
          + "    \"description\": \"승인됨\"\n"
          + "  }\n"
          + "}\n";
}
