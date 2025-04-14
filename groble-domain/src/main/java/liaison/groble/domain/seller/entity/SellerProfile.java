package liaison.groble.domain.seller.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.seller.enums.SellerStatus;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seller_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerProfile {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  // 본인인증 정보
  @Column(nullable = false)
  private String verifiedPhoneNumber; // 인증된 전화번호

  @Column(nullable = false)
  private String verifiedName; // 인증된 이름

  @Column(nullable = false)
  private LocalDateTime verifiedAt; // 인증 시간

  @Column(length = 1000)
  private String verificationData; // NICE 인증 응답 데이터 (필요한 경우)

  // 판매자 상태
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SellerStatus sellerStatus = SellerStatus.PENDING;

  @Builder
  private SellerProfile(
      User user,
      String verifiedPhoneNumber,
      String verifiedName,
      LocalDateTime verifiedAt,
      String verificationData) {
    this.user = user;
    this.verifiedPhoneNumber = verifiedPhoneNumber;
    this.verifiedName = verifiedName;
    this.verifiedAt = verifiedAt;
    this.verificationData = verificationData;
  }
}
