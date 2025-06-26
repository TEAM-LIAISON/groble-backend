package liaison.groble.application.market.dto;

import java.util.List;

import liaison.groble.domain.user.entity.SellerContact;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactInfoDTO {
  private String instagram;
  private String email;
  private String openChat;
  private String etc;

  public static ContactInfoDTO from(List<SellerContact> contacts) {
    ContactInfoDTOBuilder builder = ContactInfoDTO.builder();
    for (SellerContact contact : contacts) {
      switch (contact.getContactType()) {
        case INSTAGRAM -> builder.instagram(contact.getContactValue());
        case EMAIL -> builder.email(contact.getContactValue());
        case OPENCHAT -> builder.openChat(contact.getContactValue());
        case ETC -> builder.etc(contact.getContactValue());
      }
    }
    return builder.build();
  }
}
