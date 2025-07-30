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
import liaison.groble.domain.user.exception.InvalidContactException;

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
  private static final int MAX_CONTACT_LENGTH = 500;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 연관된 판매자 정보 (User or SellerInfo 등) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /** INSTAGRAM, EMAIL, OPENCHAT 등 enum */
  @Enumerated(EnumType.STRING)
  @Column(name = "contact_type", nullable = false, length = 20)
  private ContactType contactType;

  /** 실제 URL 또는 이메일 주소 */
  @Column(name = "contact_value", nullable = false, length = 500)
  private String contactValue;

  // 연락처 값을 변경하는 메소드
  public void changeContactValue(String contactValue) {
    validateContactValue(contactValue);
    this.contactValue = contactValue;
  }

  private static void validateContactValue(String value) {
    if (value == null || value.isBlank()) {
      throw new InvalidContactException("연락처 정보는 필수입니다");
    }
    if (value.length() > MAX_CONTACT_LENGTH) {
      throw new InvalidContactException("연락처는 500자를 초과할 수 없습니다");
    }
  }
}
