package liaison.groble.api.model.admin.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 페이지에서 홈 테스트 연락처 정보 응답 DTO")
public class AdminHomeTestContactResponse {

  @Schema(description = "홈 테스트 연락처 ID", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long id;

  @Schema(description = "연락처 생성 시각", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @Schema(description = "연락처 최근 수정 시각", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  @Schema(
      description = "연락처 전화번호",
      example = "010-9876-5432",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String phoneNumber;

  @Schema(
      description = "연락처 이메일",
      example = "contact@example.com",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String email;

  @Schema(description = "연락처 닉네임", example = "테스터", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String nickname;
}
