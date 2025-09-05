package liaison.groble.api.server.common.swagger;

public final class SwaggerTags {

  private SwaggerTags() {}

  public static final class Guest {
    public static final String AUTH = "[👀 비회원] 비회원 인증/인가 기능";
    public static final String AUTH_DESC = "비회원 토큰 발급, 비회원 전화번호 인증/검증";

    private Guest() {}
  }

  public static final class Purchase {
    public static final String REVIEW = "[🧾 통합 리뷰 관리] 회원/비회원 구매자 리뷰 작성, 수정, 삭제 API";
    public static final String REVIEW_DESC = "회원/비회원 구매자 리뷰 작성, 수정, 삭제 기능을 제공합니다.";
  }

  public static final class Sell {
    public static final String SELL_CONTENT = "[🎁 상품 관리 | 판매 관리] 상품 관리 내 판매 관리 기능";
    public static final String SELL_CONTENT_DESC = "특정 콘텐츠 판매 정보에 대한 모든 기능을 제공합니다.";
  }

  public static final class Payment {
    public static final String PAYPLE =
        "[💰 페이플 결제] 회원/비회원 앱카드 결제 진행, 회원 정기(빌링) 결제 진행, 결제 취소 기능 API";
    public static final String PAYPLE_DESC =
        "토큰 종류에 따라 회원/비회원을 자동 판단하여 앱카드 결제 및 정기(빌링) 결제를 진행하고, 결제 취소 기능을 제공합니다.";
    public static final String GENERAL = "[💳 일반 결제] 통합 결제 관리 API";
    public static final String GENERAL_DESC = "결제 조회, 검증, 환불 등 결제 관련 전반적인 기능을 제공합니다.";

    private Payment() {}
  }

  public static final class User {
    public static final String PROFILE = "[👤 프로필] 사용자 프로필 관리 API";
    public static final String PROFILE_DESC = "사용자 프로필 조회, 수정, 이미지 업로드 등의 기능을 제공합니다.";
    public static final String AUTH = "[🔐 인증] 사용자 인증/인가 API";
    public static final String AUTH_DESC = "로그인, 로그아웃, 토큰 발급 등 인증 관련 기능을 제공합니다.";
    public static final String MY_PAGE = "[👨‍💻 마이페이지] 마이페이지 관련 API";
    public static final String MY_PAGE_DESC = "마이페이지 조회, 설정 변경 등의 기능을 제공합니다.";

    private User() {}
  }

  public static final class Content {
    public static final String MANAGEMENT = "[📝 콘텐츠] 콘텐츠 관리 API";
    public static final String MANAGEMENT_DESC = "콘텐츠 조회, 등록, 수정, 삭제 등의 기능을 제공합니다.";
    public static final String PURCHASE = "[🛒 구매] 콘텐츠 구매 관련 API";
    public static final String PURCHASE_DESC = "콘텐츠 구매, 구매 내역 조회 등의 기능을 제공합니다.";

    private Content() {}
  }

  public static final class Market {
    public static final String MANAGEMENT = "[🏷 마켓 관리] 마켓 관리 및 마켓 뷰어 API";
    public static final String MANAGEMENT_DESC = "마켓 관리 및 마켓 뷰어 화면에서 사용되는 모든 기능을 제공합니다.";

    private Market() {}
  }

  public static final class Admin {
    public static final String AUTH = "[✅ 관리자 인증/인가] 관리자 로그인 및 로그아웃";
    public static final String AUTH_DESC = "관리자의 로그인 및 로그아웃 기능을 제공합니다.";

    public static final String DASHBOARD = "[✅ 관리자 대시보드] 관리자 대시보드 API";
    public static final String DASHBOARD_DESC = "관리자 대시보드에 필요한 통계,요약,알림 정보를 제공합니다.";

    public static final String USER = "[✅ 관리자 사용자 관리] 사용자 관리 API";
    public static final String USER_DESC = "관리자의 사용자 관리 기능을 제공합니다.";
    public static final String CONTENT = "[✅ 관리자 콘텐츠 관리] 콘텐츠 관리 API";
    public static final String CONTENT_DESC = "관리자의 콘텐츠 관리 및 모니터링 기능을 제공합니다.";

    public static final String SETTLEMENT = "[✅ 관리자 정산 관리] 정산 관리 API";
    public static final String SETTLEMENT_DESC = "정산 내역 조회 및 정산 완료 기능을 제공합니다.";

    private Admin() {}
  }

  public static final class Common {
    public static final String HEALTH = "[🏥 상태 확인] 서버 상태 확인 API";
    public static final String HEALTH_DESC = "서버 상태 및 헬스체크 기능을 제공합니다.";
    public static final String GLOBAL = "[💿 동적 데이터] 전역 데이터 API";
    public static final String GLOBAL_DESC = "SEO 동적 데이터 및 전역 설정 정보를 제공합니다.";

    private Common() {}
  }

  public static final class Order {
    public static final String ORDER = "[🔄 통합 주문] 회원/비회원 통합 주문 발행, 회원/비회원 주문 결과 조회 API";
    public static final String ORDER_DESC = "회원/비회원 통합 주문 발행, 회원/비회원 주문 결과 조회 기능을 제공합니다.";

    private Order() {}
  }
}
