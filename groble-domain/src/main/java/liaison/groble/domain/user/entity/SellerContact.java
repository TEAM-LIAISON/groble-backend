package liaison.groble.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.user.enums.ContactType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "seller_contacts",
    indexes = {
      @Index(name = "idx_seller_contacts_user_id", columnList = "user_id"),
      @Index(name = "idx_seller_contacts_contact_type", columnList = "contact_type"),
      @Index(
          name = "idx_seller_contacts_contact_type_user_id",
          columnList = "contact_type, user_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SellerContact {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** INSTAGRAM, EMAIL, OPENCHAT 등 enum */
  @Enumerated(EnumType.STRING)
  @Column(name = "contact_type", nullable = false)
  private ContactType contactType;

  /** 실제 URL 또는 이메일 주소 */
  @Column(name = "contact_value", columnDefinition = "TEXT", nullable = false)
  private String contactValue;

  /** 연관된 판매자 정보 (User or SellerInfo 등) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id") // 또는 seller_info_id, 상황에 따라 다름
  private User user;

  // 업데이트 메서드 추가
  public void updateContactValue(String contactValue) {
    this.contactValue = contactValue;
  }
}
