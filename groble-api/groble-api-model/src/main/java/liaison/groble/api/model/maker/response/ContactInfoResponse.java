package liaison.groble.api.model.maker.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import liaison.groble.domain.user.entity.SellerContact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactInfoResponse {
  private String instagram;
  private String email;
  private String openChat;
  private String etc;

  public static ContactInfoResponse from(List<SellerContact> contacts) {
    ContactInfoResponseBuilder builder = ContactInfoResponse.builder();
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
