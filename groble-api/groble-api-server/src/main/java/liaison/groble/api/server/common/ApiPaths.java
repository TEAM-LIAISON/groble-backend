package liaison.groble.api.server.common;

public final class ApiPaths {

  private ApiPaths() {}

  public static final String API_V1 = "/api/v1";

  public static final class Admin {
    public static final String BASE = API_V1 + "/admin";

    public static final String AUTH_BASE = BASE + "/auth";
    public static final String AUTH_SIGN_IN = "/sign-in";
    public static final String AUTH_LOGOUT = "/logout";

    // 어드민 홈 탭
    public static final String DASHBOARD_BASE = BASE + "/dashboard";
    public static final String DASHBOARD_OVERVIEW = "/overview";
    public static final String DASHBOARD_TRENDS = "/trends";
    public static final String DASHBOARD_TOP_CONTENTS = "/top-contents";

    // 어드민 사용자 탭
    public static final String ADMIN_USER_SUMMARY_INFO = "/users";
    public static final String ADMIN_GUEST_USER_SUMMARY_INFO = "/guest-users";
    public static final String ADMIN_HOME_TEST_CONTACTS = "/home-test-contacts";
    public static final String ADMIN_USER_ACCOUNT_VERIFICATION =
        "/users/{nickname}/account-verification";
    public static final String ADMIN_USER_BUSINESS_INFO = "/users/{userId}/business-info";

    public static final String CONTENTS = "/contents";
    public static final String CONTENT_EXAMINE = "/content/{contentId}/examine";

    public static final String SETTLEMENT_BASE = BASE + "/settlements";

    public static final String APPROVE_SETTLEMENTS = "/approve";

    public static final String ALL_USERS_SETTLEMENTS = "/all-users";
    public static final String SETTLEMENT_DETAIL = "/{settlementId}";
    public static final String SALES_LIST = "/sales/{settlementId}";

    public static final String ORDERS = "/orders";
    public static final String ORDER_CANCEL_REQUEST = "/order/{merchantUid}/cancel-request";
    public static final String ORDER_CANCELLATION_REASON =
        "/order/{merchantUid}/cancellation-reason";

    public static final String MAKER_BASE = BASE + "/maker";
    public static final String MAKER_DETAIL = "/{nickname}";
    public static final String MAKER_VERIFY = "/verify";
    public static final String MAKER_MEMO = "/memo/{nickname}";

    private Admin() {}
  }

  public static final class Auth {
    public static final String ACCOUNT_VERIFICATION = API_V1 + "/account-verification";
    public static final String PERSONAL_MAKER_VERIFICATION = "/personal-maker";
    public static final String BUSINESS_MAKER_BANKBOOK_VERIFICATION = "/business-maker";
    public static final String BUSINESS_MAKER_VERIFICATION = "/business";

    public static final String UPLOAD_BANKBOOK_COPY = "/upload-bankbook-copy";
    public static final String UPLOAD_BUSINESS_LICENSE = "/upload-business-license";

    private Auth() {}
  }

  public static final class Guest {
    public static final String BASE_AUTH = API_V1 + "/guest/auth";

    public static final String PHONE_CODE_REQUEST = "/code-request";
    public static final String PHONE_CODE_VERIFY = "/verify-request";
    public static final String UPDATE_GUEST_USER_INFO = "/update-info";
  }

  public static final class Payment {
    public static final String BASE = API_V1 + "/payments";
    public static final String PAYPLE_BASE = BASE + "/payple";

    public static final String APP_CARD_REQUEST = "/app-card/request";
    public static final String CANCEL = "/{merchantUid}/cancel";
    public static final String VERIFICATION = "/verification";
    public static final String REFUND = "/refund";

    private Payment() {}
  }

  public static final class User {
    public static final String BASE = API_V1 + "/users";

    public static final String SWITCH_ROLE = BASE + "/switch-role";
    public static final String MY_PAGE_SUMMARY = BASE + "/me/summary";
    public static final String MY_PAGE_DETAIL = BASE + "/me/detail";
    public static final String UPLOAD_PROFILE_IMAGE = BASE + "/me/profile-image";

    public static final String MY_PAGE = API_V1 + "/me";

    private User() {}
  }

  public static final class Content {
    public static final String BASE = API_V1 + "/contents";

    public static final String DETAIL = "/{contentId}";
    public static final String HOME_CONTENTS = API_V1 + "/home/contents";
    public static final String THUMBNAIL = "/thumbnail/image";
    public static final String DETAIL_IMAGES = "/detail/images";
    public static final String REVIEWS = "/{contentId}/reviews";

    private Content() {}
  }

  public static final class Market {
    public static final String BASE = API_V1 + "/market";

    public static final String EDIT_INTRO = "/edit/intro";
    public static final String INTRO = "/intro/{marketLinkUrl}";
    public static final String CONTENTS = "/contents/{marketLinkUrl}";
    public static final String EDIT = "/edit";
    public static final String LINK_CHECK = "/link-check";

    private Market() {}
  }

  public static final class Order {
    public static final String BASE = API_V1 + "/orders";
    public static final String CREATE_ORDER = "/create";
    public static final String GET_ORDER_SUCCESS = "/success/{merchantUid}";
  }

  public static final class HomeTest {
    public static final String BASE = API_V1 + "/home/payment-test";
    public static final String SEND_CODE = "/code-request";
    public static final String VERIFY_CODE = "/verify-request";
    public static final String SAVE_EMAIL = "/email";
    public static final String COMPLETE = "/complete";

    private HomeTest() {}
  }

  public static final class Purchase {
    public static final String BASE = API_V1 + "/purchase";

    public static final String REVIEW_BASE = BASE + "/review";
    public static final String ADD_REVIEW = "/{merchantUid}";
    public static final String UPDATE_REVIEW = "/update/{reviewId}";
    public static final String DELETE_REVIEW = "/delete/{reviewId}";
  }

  public static final class Sell {
    public static final String BASE = API_V1 + "/sell";

    public static final String SELL_CONTENT_BASE = BASE + "/content/manage";

    public static final String CONTENT_HOME = "/{contentId}";
    public static final String CONTENT_SELL_LIST = "/{contentId}/sell-list";
    public static final String CONTENT_SELL_DETAIL = "/{contentId}/sell-detail/{purchaseId}";
    public static final String CONTENT_REVIEW_LIST = "/{contentId}/review-list";
    public static final String CONTENT_REVIEW_DETAIL = "/{contentId}/review-detail/{reviewId}";
    public static final String DELETE_REVIEW_REQUEST = "/{reviewId}/review-delete-request";
    public static final String ADD_REVIEW_REPLY = "/{reviewId}/review-reply";
    public static final String UPDATE_REVIEW_REPLY = "/{reviewId}/review-reply/{replyId}";
    public static final String DELETE_REVIEW_REPLY = "/{reviewId}/review-reply/{replyId}/delete";

    private Sell() {}
  }
}
