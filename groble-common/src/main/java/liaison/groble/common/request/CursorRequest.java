package liaison.groble.common.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 커서 기반 페이지네이션을 위한 요청 클래스 클라이언트에서 페이지네이션 요청 시 사용 */
@Schema(description = "커서 기반 페이지네이션 요청")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorRequest {

  @Schema(description = "다음 페이지 요청에 사용할 커서 (첫 페이지는 null 또는 빈 문자열)", example = "12345")
  private String cursor;

  @Schema(description = "요청 페이지 크기 (최소 1)", example = "10", required = true, minimum = "1")
  @NotNull(message = "페이지 크기는 필수입니다")
  @Min(value = 1, message = "페이지 크기는 최소 1 이상이어야 합니다")
  private Integer size;

  @Schema(description = "정렬 기준 필드명", example = "createdAt")
  private String sortBy;

  @Schema(description = "내림차순 정렬 여부 (true: 내림차순, false: 오름차순)", example = "true")
  private Boolean sortDesc;

  /**
   * 기본 요청 생성
   *
   * @param cursor 다음 페이지 요청에 사용할 커서
   * @param size 요청 페이지 크기
   * @return 커서 기반 페이지네이션 요청
   */
  public static CursorRequest of(String cursor, int size) {
    return CursorRequest.builder().cursor(cursor).size(size).build();
  }

  /**
   * 정렬 정보를 포함한 요청 생성
   *
   * @param cursor 다음 페이지 요청에 사용할 커서
   * @param size 요청 페이지 크기
   * @param sortBy 정렬 기준
   * @param sortDesc 내림차순 정렬 여부
   * @return 커서 기반 페이지네이션 요청
   */
  public static CursorRequest of(String cursor, int size, String sortBy, boolean sortDesc) {
    return CursorRequest.builder()
        .cursor(cursor)
        .size(size)
        .sortBy(sortBy)
        .sortDesc(sortDesc)
        .build();
  }

  /**
   * 요청이 첫 페이지인지 확인
   *
   * @return 첫 페이지 여부
   */
  public boolean isFirst() {
    return cursor == null || cursor.isBlank();
  }
}
