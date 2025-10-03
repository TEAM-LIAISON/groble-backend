package liaison.groble.api.server.common;

public final class ResponseMessages {

  private ResponseMessages() {}

  public static final class Admin {
    public static final String DASHBOARD_OVERVIEW_RETRIEVED = "관리자 대시보드 개요 조회에 성공하였습니다.";
    public static final String SETTLEMENT_APPROVAL_SUCCESS = "정산 승인 요청이 성공적으로 처리되었습니다.";

    public static final String ALL_USERS_SETTLEMENTS_RETRIEVED = "전체 사용자 정산 내역 조회에 성공하였습니다.";
    public static final String SETTLEMENT_DETAIL_RETRIEVED = "사용자 정산 상세 내역 조회에 성공하였습니다.";
    public static final String SALES_LIST_RETRIEVED = "정산별 판매 내역 조회에 성공하였습니다.";
    public static final String USER_SUMMARY_INFO_RETRIEVED = "관리자 전체 사용자 목록 조회에 성공하였습니다.";
    public static final String ADMIN_SIGN_IN_SUCCESS = "관리자 로그인에 성공했습니다.";
    public static final String ADMIN_LOGOUT_SUCCESS = "관리자 로그아웃이 성공적으로 처리되었습니다.";
    public static final String CONTENT_SUMMARY_INFO_RETRIEVED = "관리자 콘텐츠 목록 조회에 성공하였습니다.";
    public static final String CONTENT_EXAMINE_APPROVED = "콘텐츠 심사 승인에 성공했습니다.";
    public static final String CONTENT_EXAMINE_REJECTED = "콘텐츠 심사 반려에 성공했습니다.";
    public static final String ORDER_SUMMARY_INFO_RETRIEVED = "모든 주문 목록을 조회하는 데 성공했습니다.";
    public static final String ORDER_CANCEL_REQUEST_PROCESSED =
        "결제 취소 요청 주문에 대한 승인 및 거절 처리에 성공했습니다.";
    public static final String ORDER_CANCELLATION_REASON_RETRIEVED = "결제 취소 주문에 대한 사유 조회에 성공했습니다.";
    public static final String MAKER_DETAIL_INFO_RETRIEVED = "메이커 상세 정보 조회에 성공했습니다.";
    public static final String MAKER_VERIFY_APPROVED = "메이커 인증 승인에 성공했습니다.";
    public static final String MAKER_VERIFY_REJECTED = "메이커 인증 거절에 성공했습니다.";
    public static final String MAKER_MEMO_SAVED = "관리자 메모 저장에 성공했습니다.";
    public static final String USER_ACCOUNT_VERIFICATION_SUCCESS = "관리자 계좌 인증이 성공적으로 완료되었습니다.";
    public static final String USER_ACCOUNT_VERIFICATION_FAILED = "관리자 계좌 인증 처리 결과가 실패했습니다.";
    public static final String USER_BUSINESS_INFO_UPDATED = "사업자 정보가 성공적으로 수정되었습니다.";
  }

  public static final class Auth {
    public static final String PERSONAL_MAKER_VERIFICATION_SUCCESS = "개인 메이커 인증 요청이 성공적으로 처리되었습니다.";
    public static final String BUSINESS_MAKER_BANKBOOK_VERIFICATION_SUCCESS =
        "개인 • 법인 사업자 메이커 계좌 및 통장 사본 저장 요청이 성공적으로 처리되었습니다.";
    public static final String BUSINESS_MAKER_VERIFICATION_SUCCESS =
        "개인 • 법인 사업자 메이커 인증 요청이 성공적으로 처리되었습니다.";
    public static final String LOGOUT_SUCCESS = "로그아웃이 성공적으로 처리되었습니다.";
    public static final String GUEST_LOGOUT_SUCCESS = "게스트 로그아웃이 성공적으로 처리되었습니다.";
  }

  public static final class Content {
    public static final String UPLOAD_SUCCESS = "파일 업로드가 성공적으로 완료되었습니다.";
    public static final String DETAIL_RETRIEVED = "콘텐츠 상세 조회에 성공하였습니다.";
    public static final String LIST_RETRIEVED = "콘텐츠 목록 조회에 성공하였습니다.";

    private Content() {}
  }

  public static final class Guest {
    public static final String GUEST_AUTH_PHONE_REQUEST_SUCCESS = "비회원 전화번호 인증 요청이 성공적으로 완료되었습니다.";
    public static final String VERIFY_GUEST_AUTH_PHONE_SUCCESS = "비회원 전화번호 인증이 성공적으로 완료되었습니다.";
    public static final String UPDATE_GUEST_USER_INFO_SUCCESS = "비회원 사용자 정보 업데이트가 성공적으로 완료되었습니다.";
  }

  public static final class Order {
    public static final String ORDER_CREATE_SUCCESS = "주문 생성이 성공적으로 완료되었습니다.";
    public static final String GET_ORDER_SUCCESS = "주문 성공 페이지 정보 조회에 성공하였습니다.";

    private Order() {}
  }

  public static final class Payment {
    public static final String REQUEST_SUCCESS = "결제 승인 요청이 성공적으로 처리되었습니다.";
    public static final String CANCEL_SUCCESS = "결제 취소 요청이 성공적으로 처리되었습니다.";
    public static final String VERIFICATION_SUCCESS = "결제 검증이 성공적으로 완료되었습니다.";
    public static final String REFUND_SUCCESS = "환불 요청이 성공적으로 처리되었습니다.";

    private Payment() {}
  }

  public static final class Purchase {
    public static final String PURCHASE_REVIEW_ADD_SUCCESS = "구매자가 콘텐츠에 대한 리뷰 작성에 성공했습니다.";
    public static final String PURCHASE_REVIEW_UPDATE_SUCCESS = "구매자가 콘텐츠에 대한 리뷰 수정에 성공했습니다.";
    public static final String PURCHASE_REVIEW_DELETE_SUCCESS = "구매자가 콘텐츠에 대한 리뷰 삭제에 성공했습니다.";

    private Purchase() {}
  }

  public static final class Sell {
    public static final String SELL_CONTENT_HOME_SUCCESS = "판매 관리 메인 페이지 조회에 성공했습니다.";
    public static final String SELL_CONTENT_LIST_SUCCESS = "판매 리스트 전체보기에 성공하였습니다.";
    public static final String SELL_CONTENT_DETAIL_SUCCESS = "판매 콘텐츠 상세 조회에 성공하였습니다.";

    public static final String SELL_CONTENT_REVIEW_LIST_SUCCESS = "판매 콘텐츠 리뷰 리스트 조회에 성공하였습니다.";
    public static final String SELL_CONTENT_REVIEW_DETAIL_SUCCESS = "판매 콘텐츠 리뷰 상세 조회에 성공하였습니다.";
    public static final String SELL_CONTENT_REVIEW_DELETE_SUCCESS = "판매 콘텐츠 리뷰 삭제에 성공하였습니다.";

    public static final String REVIEW_REPLY_ADD_SUCCESS = "리뷰 답글 작성에 성공하였습니다.";
    public static final String REVIEW_REPLY_UPDATE_SUCCESS = "리뷰 답글 수정에 성공하였습니다.";
    public static final String REVIEW_REPLY_DELETE_SUCCESS = "리뷰 답글 삭제에 성공하였습니다.";

    private Sell() {}
  }

  public static final class User {
    public static final String USER_HEADER_INFORM_SUCCESS = "사용자 헤더 정보 조회에 성공했습니다.";
    public static final String MY_PAGE_SUMMARY_SUCCESS = "마이페이지 요약 정보 조회에 성공했습니다.";
    public static final String MY_PAGE_DETAIL_SUCCESS = "마이페이지 상세 정보 조회에 성공했습니다.";
    public static final String PROFILE_IMAGE_UPLOAD_SUCCESS = "프로필 이미지가 성공적으로 업로드되었습니다.";
    public static final String PROFILE_UPDATED = "프로필이 성공적으로 수정되었습니다.";
    public static final String ROLE_SWITCHED = "가입 유형이 전환되었습니다.";
    public static final String PASSWORD_CHANGED = "비밀번호가 성공적으로 변경되었습니다.";

    private User() {}
  }

  public static final class HomeTest {
    public static final String PHONE_AUTH_CODE_SENT = "테스트용 인증 코드 발송이 완료되었습니다.";
    public static final String PHONE_AUTH_VERIFIED = "테스트용 전화번호 인증이 완료되었습니다.";
    public static final String PHONE_AUTH_EMAIL_SAVED = "테스트용 이메일 정보가 저장되었습니다.";
    public static final String PHONE_AUTH_COMPLETED = "테스트 결제 체험이 완료되었습니다.";

    private HomeTest() {}
  }
}
