package liaison.groble.domain.user.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserProfile {
  @Column(name = "nickname", length = 50)
  private String nickname;

  @Lob
  @Column(name = "profile_image_url", columnDefinition = "TEXT")
  private String profileImageUrl;

  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void updateProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public void updatePhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void anonymize() {
    this.nickname = "탈퇴한 사용자";
    this.profileImageUrl = null;
    this.phoneNumber = null;
  }
}
