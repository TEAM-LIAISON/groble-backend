package liaison.groble.api.server.admin.docs;

import liaison.groble.api.server.common.swagger.SwaggerTags;

public final class AdminNotificationSwaggerDocs {

  private AdminNotificationSwaggerDocs() {}

  public static final String TAG_NAME = SwaggerTags.Admin.NOTIFICATION;
  public static final String TAG_DESCRIPTION = SwaggerTags.Admin.NOTIFICATION_DESC;

  public static final String CREATE_SCHEDULED_NOTIFICATION_SUMMARY = "[✅ 관리자 알림] 예약 알림 생성";
  public static final String CREATE_SCHEDULED_NOTIFICATION_DESCRIPTION =
      "관리자가 시스템/카카오 채널에 대한 예약 알림을 등록합니다.";

  public static final String GET_SCHEDULED_NOTIFICATIONS_SUMMARY = "[✅ 관리자 알림] 예약 알림 목록 조회";
  public static final String GET_SCHEDULED_NOTIFICATIONS_DESCRIPTION = "페이징으로 예약 알림 목록을 조회합니다.";

  public static final String UPDATE_SCHEDULED_NOTIFICATION_SUMMARY = "[✅ 관리자 알림] 예약 알림 수정";
  public static final String UPDATE_SCHEDULED_NOTIFICATION_DESCRIPTION = "등록된 예약 알림 정보를 수정합니다.";

  public static final String CANCEL_SCHEDULED_NOTIFICATION_SUMMARY = "[✅ 관리자 알림] 예약 알림 취소";
  public static final String CANCEL_SCHEDULED_NOTIFICATION_DESCRIPTION = "발송 대기 중인 예약 알림을 취소합니다.";

  public static final String GET_SEGMENTS_SUMMARY = "[✅ 관리자 알림] 세그먼트 목록 조회";
  public static final String GET_SEGMENTS_DESCRIPTION = "사전 정의된 세그먼트와 커스텀 세그먼트를 조회합니다.";

  public static final String GET_KAKAO_TEMPLATES_SUMMARY = "[✅ 관리자 알림] 카카오 알림톡 템플릿 조회";
  public static final String GET_KAKAO_TEMPLATES_DESCRIPTION = "비즈뿌리오에 등록된 템플릿 코드를 조회합니다.";

  public static final String GET_STATISTICS_SUMMARY = "[✅ 관리자 알림] 예약 알림 통계 조회";
  public static final String GET_STATISTICS_DESCRIPTION = "예약 알림의 상태별 통계를 조회합니다.";
}
