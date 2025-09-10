package liaison.groble.application.auth.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 은행명을 기관 코드로 변환하는 유틸리티 클래스
 *
 * <p>페이플에서 제공하는 금융 기관명을 금융결제원 기관 코드로 매핑합니다.
 */
public final class BankCodeUtil {

  private BankCodeUtil() {}

  /** 은행명 -> 기관 코드 매핑 테이블 */
  private static final Map<String, String> BANK_CODE_MAP = new HashMap<>();

  static {
    // 주요 시중은행
    BANK_CODE_MAP.put("KB국민은행", "004");
    BANK_CODE_MAP.put("우리은행", "020");
    BANK_CODE_MAP.put("SC제일은행", "023");
    BANK_CODE_MAP.put("한국씨티은행", "027");
    BANK_CODE_MAP.put("대구은행", "031");
    BANK_CODE_MAP.put("하나은행", "081");
    BANK_CODE_MAP.put("신한은행", "088");

    // 인터넷 전문은행
    BANK_CODE_MAP.put("케이뱅크", "089");
    BANK_CODE_MAP.put("토스뱅크", "092");

    // 특수은행 및 농협
    BANK_CODE_MAP.put("수협은행(수협중앙회)", "007");
    BANK_CODE_MAP.put("NH농협은행", "011");
    BANK_CODE_MAP.put("IBK기업은행", "003");
    BANK_CODE_MAP.put("KDB산업은행", "002");

    // 지방은행
    BANK_CODE_MAP.put("부산은행", "032");
    BANK_CODE_MAP.put("경남은행", "039");
    BANK_CODE_MAP.put("광주은행", "034");
    BANK_CODE_MAP.put("전북은행", "037");
    BANK_CODE_MAP.put("제주은행", "035");
  }

  /**
   * 은행명으로 기관 코드를 조회합니다.
   *
   * @param bankName 페이플에서 제공하는 금융 기관명
   * @return 금융결제원 기관 코드 (3자리), 매핑되지 않은 경우 null
   */
  public static String getBankCode(String bankName) {
    if (bankName == null || bankName.trim().isEmpty()) {
      return null;
    }
    return BANK_CODE_MAP.get(bankName.trim());
  }

  /**
   * 은행명이 지원되는 은행인지 확인합니다.
   *
   * @param bankName 페이플에서 제공하는 금융 기관명
   * @return 지원 여부
   */
  public static boolean isSupportedBank(String bankName) {
    return getBankCode(bankName) != null;
  }

  /**
   * 지원되는 모든 은행명을 반환합니다.
   *
   * @return 지원되는 은행명 집합
   */
  public static Set<String> getSupportedBankNames() {
    return BANK_CODE_MAP.keySet();
  }
}
