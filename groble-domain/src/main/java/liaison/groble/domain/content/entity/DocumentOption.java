package liaison.groble.domain.content.entity;

import static jakarta.persistence.EnumType.STRING;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;

import liaison.groble.domain.content.enums.ContentDeliveryMethod;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DOCUMENT")
@Getter
@NoArgsConstructor
public class DocumentOption extends ContentOption {
  // 콘텐츠 제공 방식
  @Enumerated(value = STRING)
  private ContentDeliveryMethod contentDeliveryMethod;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String documentOriginalFileName;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String documentFileUrl;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String documentLinkUrl;

  // Setter 메서드 추가
  public void setContentDeliveryMethod(ContentDeliveryMethod contentDeliveryMethod) {
    this.contentDeliveryMethod = contentDeliveryMethod;
  }

  public void setDocumentOriginalFileName(String documentOriginalFileName) {
    this.documentOriginalFileName = documentOriginalFileName;
  }

  public void setDocumentFileUrl(String documentFileUrl) {
    this.documentFileUrl = documentFileUrl;
  }

  public void setDocumentLinkUrl(String documentLinkUrl) {
    this.documentLinkUrl = documentLinkUrl;
  }
}
