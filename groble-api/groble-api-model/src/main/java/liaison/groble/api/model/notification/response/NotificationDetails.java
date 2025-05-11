package liaison.groble.api.model.notification.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@Schema(description = "알림 상세 응답")
public class NotificationDetails {

  // 회원 닉네임
  private String nickname;

  // 시스템 알림
  private String systemTitle;

  // 1:1 문의 요청자 이름
  private String inquiryRequesterName;
  // 1:1 문의가 들어간 콘텐츠의 썸네일 URL
  private String contentThumbnailUrl;

  @Builder
  private NotificationDetails(
      final String nickname,
      final String systemTitle,
      final String inquiryRequesterName,
      final String contentThumbnailUrl) {
    this.nickname = nickname;
    this.systemTitle = systemTitle;
    this.inquiryRequesterName = inquiryRequesterName;
    this.contentThumbnailUrl = contentThumbnailUrl;
  }

  // 그로블 환영 알림
  public static NotificationDetails welcomeGroble(final String nickname, final String systemTitle) {
    return NotificationDetails.builder().nickname(nickname).systemTitle(systemTitle).build();
  }
}
