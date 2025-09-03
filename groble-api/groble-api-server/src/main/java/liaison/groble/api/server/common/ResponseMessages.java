package liaison.groble.api.server.common;

public final class ResponseMessages {

  private ResponseMessages() {}

  public static final class Admin {
    public static final String SETTLEMENT_APPROVAL_SUCCESS = "정산 승인 요청이 성공적으로 처리되었습니다.";
  }

  public static final class Payment {
    public static final String REQUEST_SUCCESS = "결제 승인 요청이 성공적으로 처리되었습니다.";
    public static final String CANCEL_SUCCESS = "결제 취소 요청이 성공적으로 처리되었습니다.";
    public static final String VERIFICATION_SUCCESS = "결제 검증이 성공적으로 완료되었습니다.";
    public static final String REFUND_SUCCESS = "환불 요청이 성공적으로 처리되었습니다.";

    private Payment() {}
  }

  public static final class Common {
    public static final String SUCCESS = "요청이 성공적으로 처리되었습니다.";
    public static final String CREATED = "성공적으로 생성되었습니다.";
    public static final String UPDATED = "성공적으로 수정되었습니다.";
    public static final String DELETED = "성공적으로 삭제되었습니다.";
    public static final String RETRIEVED = "조회에 성공하였습니다.";

    private Common() {}
  }

  public static final class User {
    public static final String PROFILE_UPDATED = "프로필이 성공적으로 수정되었습니다.";
    public static final String ROLE_SWITCHED = "가입 유형이 전환되었습니다.";
    public static final String PASSWORD_CHANGED = "비밀번호가 성공적으로 변경되었습니다.";

    private User() {}
  }

  public static final class Content {
    public static final String UPLOAD_SUCCESS = "파일 업로드가 성공적으로 완료되었습니다.";
    public static final String DETAIL_RETRIEVED = "콘텐츠 상세 조회에 성공하였습니다.";
    public static final String LIST_RETRIEVED = "콘텐츠 목록 조회에 성공하였습니다.";

    private Content() {}
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
}
