package liaison.groble.common.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커서 기반 페이지네이션을 위한 응답 클래스 무한 스크롤 또는 "더보기" 기능에 적합한 구조
 *
 * @param <T> 응답 데이터 아이템의 타입
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CursorResponse<T> {

  private List<T> items; // 현재 페이지의 아이템 목록
  private String nextCursor; // 다음 페이지 요청에 사용할 커서 (null이면 마지막 페이지)
  private boolean hasNext; // 다음 페이지 존재 여부
  private int totalCount; // 전체 아이템 수 (선택적)
  private MetaData meta; // 추가 메타데이터 (선택적)

  /**
   * 기본 커서 응답 생성
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @param items 현재 페이지의 아이템 목록
   * @param nextCursor 다음 페이지 요청에 사용할 커서
   * @param hasNext 다음 페이지 존재 여부
   * @return 커서 기반 페이지네이션 응답
   */
  public static <T> CursorResponse<T> of(List<T> items, String nextCursor, boolean hasNext) {
    return CursorResponse.<T>builder().items(items).nextCursor(nextCursor).hasNext(hasNext).build();
  }

  /**
   * 전체 개수를 포함한 커서 응답 생성
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @param items 현재 페이지의 아이템 목록
   * @param nextCursor 다음 페이지 요청에 사용할 커서
   * @param hasNext 다음 페이지 존재 여부
   * @param totalCount 전체 아이템 수
   * @return 커서 기반 페이지네이션 응답
   */
  public static <T> CursorResponse<T> of(
      List<T> items, String nextCursor, boolean hasNext, int totalCount) {
    return CursorResponse.<T>builder()
        .items(items)
        .nextCursor(nextCursor)
        .hasNext(hasNext)
        .totalCount(totalCount)
        .build();
  }

  /**
   * 메타데이터를 포함한 커서 응답 생성
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @param items 현재 페이지의 아이템 목록
   * @param nextCursor 다음 페이지 요청에 사용할 커서
   * @param hasNext 다음 페이지 존재 여부
   * @param meta 추가 메타데이터
   * @return 커서 기반 페이지네이션 응답
   */
  public static <T> CursorResponse<T> of(
      List<T> items, String nextCursor, boolean hasNext, MetaData meta) {
    return CursorResponse.<T>builder()
        .items(items)
        .nextCursor(nextCursor)
        .hasNext(hasNext)
        .meta(meta)
        .build();
  }

  /**
   * 전체 응답 생성 (모든 필드 포함)
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @param items 현재 페이지의 아이템 목록
   * @param nextCursor 다음 페이지 요청에 사용할 커서
   * @param hasNext 다음 페이지 존재 여부
   * @param totalCount 전체 아이템 수
   * @param meta 추가 메타데이터
   * @return 커서 기반 페이지네이션 응답
   */
  public static <T> CursorResponse<T> of(
      List<T> items, String nextCursor, boolean hasNext, int totalCount, MetaData meta) {
    return CursorResponse.<T>builder()
        .items(items)
        .nextCursor(nextCursor)
        .hasNext(hasNext)
        .totalCount(totalCount)
        .meta(meta)
        .build();
  }

  /**
   * 빈 결과 응답 생성
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @return 빈 커서 기반 페이지네이션 응답
   */
  public static <T> CursorResponse<T> empty() {
    return CursorResponse.<T>builder().items(List.of()).hasNext(false).nextCursor(null).build();
  }

  /** 커서 응답에 포함할 추가 메타데이터 클래스 검색 관련 정보나 필터링 정보 등을 담을 수 있음 */
  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MetaData {
    private String searchTerm; // 검색어 (있는 경우)
    private String filter; // 적용된 필터 (있는 경우)
    private String sortBy; // 정렬 기준 (있는 경우)
    private String cursorType; // 커서 타입 (ID 기반, 타임스탬프 기반 등)
  }
}
