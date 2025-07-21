package liaison.groble.domain.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DOCUMENT")
@Getter
@NoArgsConstructor
public class DocumentOption extends ContentOption {

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
