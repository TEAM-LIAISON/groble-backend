package liaison.groble.api.server.common;

public final class ApiPaths {

  private ApiPaths() {}

  public static final String API_V1 = "/api/v1";

  public static final class Guest {
    public static final String BASE_AUTH = API_V1 + "/guest/auth";

    public static final String PHONE_CODE_REQUEST = BASE_AUTH + "/code-request";
    public static final String PHONE_CODE_VERIFY = BASE_AUTH + "/verify-request";
    public static final String UPDATE_GUEST_USER_INFO = BASE_AUTH + "/update-info";
  }

  public static final class Admin {
    public static final String BASE = API_V1 + "/admin";
    public static final String SETTLEMENT_BASE = BASE + "/settlements";

    private Admin() {}
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

    public static final String SWITCH_ROLE = "/switch-role";
    public static final String MY_PAGE_SUMMARY = "/me/summary";
    public static final String MY_PAGE_DETAIL = "/me/detail";
    public static final String PROFILE_IMAGE = "/profile/image";

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
