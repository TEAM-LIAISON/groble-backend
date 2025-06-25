package liaison.groble.api.model.maker.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import liaison.groble.domain.user.entity.SellerContact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(
    name = "ContactInfoResponse",
    description = "판매자의 문의처(연락처) 정보를 담은 객체",
    example =
        "{\"instagram\":\"https://insta.com/user\",\"email\":\"user@example.com\",\"openChat\":\"https://open.kakao.com/...\",\"etc\":\"기타 연락처\"}")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactInfoResponse {

  @Schema(description = "인스타그램 URL", example = "https://instagram.com/maker123")
  private String instagram;

  @Schema(description = "이메일 주소", example = "maker@example.com")
  private String email;

  @Schema(description = "카카오 오픈채팅 URL", example = "https://open.kakao.com/o/example")
  private String openChat;

  @Schema(description = "기타 연락처", example = "010-1234-5678")
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
