package liaison.groble.api.server.common;

public final class ApiPaths {

  private ApiPaths() {}

  public static final String API_V1 = "/api/v1";

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
}
