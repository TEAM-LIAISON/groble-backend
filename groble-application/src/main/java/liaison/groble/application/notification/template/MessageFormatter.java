package liaison.groble.application.notification.template;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class MessageFormatter {
  private static final Locale KOREA = Locale.KOREA;
  private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getNumberInstance(KOREA);

  // [Groble] 회원가입 완료
  public static String welcome(String userName) {
    return String.format(
        "%s님, 환영합니다🎉\n" + "그로블에 가입해 주셔서 감사합니다.\n\n" + "이제 단 5분 만에 첫 상품을 등록하고, 판매를 시작할 수 있어요.",
        userName);
  }

  // [Groble] 결제 알림
  public static String purchaseComplete(String buyerName, String contentTitle, BigDecimal price) {
    return String.format(
        "%s님, 결제가 완료되었어요!\n\n" + "- 상품명: %s\n" + "- 가격: %s원",
        buyerName, contentTitle, formatCurrency(price));
  }

  // [Groble] 판매 알림
  public static String saleComplete(String buyerName, String contentTitle, BigDecimal price) {
    return String.format(
        "%s님이 상품을 구매했어요!\n" + "\n" + "- 상품명: %s\n" + "- 가격: %s원",
        buyerName, contentTitle, formatCurrency(price));
  }

  // [Groble] 판매 중단 알림
  public static String contentDiscontinued(String sellerName, String contentTitle) {
    return String.format("%s님, 판매가 중단되었습니다.\n" + "\n" + "- 상품명: %s", sellerName, contentTitle);
  }

  // [Groble] 리뷰 등록 알림
  public static String reviewRegistered(String buyerName, String sellerName, String contentTitle) {
    return String.format(
        "%s님이 %s님의 상품에 리뷰를 남겼어요! \n" + "\n" + "- 상품명: %s", buyerName, sellerName, contentTitle);
  }

  // [Groble] 인증 완료
  public static String verificationComplete(String sellerName) {
    return String.format("%s님, 메이커 인증이 완료되었습니다!", sellerName);
  }

  // 원화 표기법 포맷팅
  private static String formatCurrency(BigDecimal amount) {
    return CURRENCY_FORMAT.format(amount);
  }
}
