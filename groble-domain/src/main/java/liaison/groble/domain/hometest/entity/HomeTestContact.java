package liaison.groble.domain.hometest.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "home_test_contacts",
    indexes = {
      @Index(name = "idx_home_test_contact_phone", columnList = "phone_number"),
      @Index(name = "idx_home_test_contact_email", columnList = "email")
    })
@NoArgsConstructor(access = PROTECTED)
public class HomeTestContact extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "phone_number", nullable = false, length = 20, unique = true)
  private String phoneNumber;

  @Column(name = "email", length = 100)
  private String email;

  @Column(name = "nickname", length = 50)
  private String nickname;

  @Builder
  private HomeTestContact(String phoneNumber, String email, String nickname) {
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.nickname = nickname;
  }

  public static HomeTestContact create(String phoneNumber, String email, String nickname) {
    return HomeTestContact.builder()
        .phoneNumber(phoneNumber)
        .email(email)
        .nickname(nickname)
        .build();
  }

  public HomeTestContact updateContactInfo(String email, String nickname) {
    this.email = email;
    if (nickname != null && !nickname.trim().isEmpty()) {
      this.nickname = nickname;
    }
    return this;
  }
}
