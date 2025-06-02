package liaison.groble.common.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 페이지 번호 기반 페이지네이션을 위한 응답 클래스 일반적인，UI에 페이지 번호를 표시하는 방식에 적합한 구조
 *
 * @param <T> 응답 데이터 아이템의 타입
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

  private List<T> items; // 현재 페이지의 아이템 목록
  private PageInfo pageInfo; // 페이지 정보
  private MetaData meta; // 추가 메타데이터 (선택적)

  /**
   * 기본 페이지 응답 생성
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @param items 현재 페이지의 아이템 목록
   * @param pageInfo 페이지 정보
   * @return 페이지 기반 페이지네이션 응답
   */
  public static <T> PageResponse<T> of(List<T> items, PageInfo pageInfo) {
    return PageResponse.<T>builder().items(items).pageInfo(pageInfo).build();
  }

  /**
   * 메타데이터를 포함한 페이지 응답 생성
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @param items 현재 페이지의 아이템 목록
   * @param pageInfo 페이지 정보
   * @param meta 추가 메타데이터
   * @return 페이지 기반 페이지네이션 응답
   */
  public static <T> PageResponse<T> of(List<T> items, PageInfo pageInfo, MetaData meta) {
    return PageResponse.<T>builder().items(items).pageInfo(pageInfo).meta(meta).build();
  }

  /**
   * 빈 결과 응답 생성
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @return 빈 페이지 기반 페이지네이션 응답
   */
  public static <T> PageResponse<T> empty() {
    return PageResponse.<T>builder()
        .items(List.of())
        .pageInfo(
            PageInfo.builder()
                .currentPage(0)
                .totalPages(0)
                .pageSize(0)
                .totalElements(0)
                .first(true)
                .last(true)
                .build())
        .build();
  }

  /**
   * Spring Data의 Page 객체로부터 PageResponse 생성
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @param <U> Page 객체의 아이템 타입
   * @param page Spring Data의 Page 객체
   * @param items 변환된 아이템 목록
   * @return 페이지 기반 페이지네이션 응답
   */
  public static <T, U> PageResponse<T> from(Page<U> page, List<T> items) {
    PageInfo pageInfo = PageInfo.fromPage(page);
    return PageResponse.<T>builder().items(items).pageInfo(pageInfo).build();
  }

  /**
   * Spring Data의 Page 객체로부터 메타데이터를 포함한 PageResponse 생성
   *
   * @param <T> 응답 데이터 아이템의 타입
   * @param <U> Page 객체의 아이템 타입
   * @param page Spring Data의 Page 객체
   * @param items 변환된 아이템 목록
   * @param meta 추가 메타데이터
   * @return 페이지 기반 페이지네이션 응답
   */
  public static <T, U> PageResponse<T> from(
      org.springframework.data.domain.Page<U> page, List<T> items, MetaData meta) {
    PageInfo pageInfo = PageInfo.fromPage(page);
    return PageResponse.<T>builder().items(items).pageInfo(pageInfo).meta(meta).build();
  }

  /** 페이지 정보를 담는 내부 클래스 */
  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class PageInfo {
    private int currentPage; // 현재 페이지 번호 (0부터 시작)
    private int totalPages; // 전체 페이지 수
    private int pageSize; // 페이지당 항목 수
    private long totalElements; // 전체 항목 수
    private boolean first; // 첫 페이지 여부
    private boolean last; // 마지막 페이지 여부
    private boolean empty; // 결과가 비어있는지 여부

    /**
     * Spring Data의 Page 객체로부터 PageInfo 생성
     *
     * @param page Spring Data의 Page 객체
     * @return 페이지 정보 객체
     */
    public static PageInfo fromPage(org.springframework.data.domain.Page<?> page) {
      return PageInfo.builder()
          .currentPage(page.getNumber())
          .totalPages(page.getTotalPages())
          .pageSize(page.getSize())
          .totalElements(page.getTotalElements())
          .first(page.isFirst())
          .last(page.isLast())
          .empty(page.isEmpty())
          .build();
    }
  }

  /** 페이지 응답에 포함할 추가 메타데이터 클래스 검색 관련 정보나 필터링 정보 등을 담을 수 있음 */
  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MetaData {
    private String searchTerm; // 검색어 (있는 경우)
    private String filter; // 적용된 필터 (있는 경우)
    private String sortBy; // 정렬 기준 (있는 경우)
    private String sortDirection; // 정렬 방향 (있는 경우)
    private List<String> categoryIds; // 카테고리 IDs (있는 경우)
  }
}
