package liaison.groble.external.infotalk.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * 파일 정보 DTO
 *
 * <p>MMS 발송 시 사용하는 첨부파일 정보입니다. 파일은 사전에 업로드 API를 통해 등록하고 받은 키를 사용합니다.
 */
@Data
@Builder
public class FileInfo {
  @JsonProperty("key")
  private String key; // 파일 키 (업로드 API 응답값)
}
