package liaison.groble.common.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
    description = "페이징 처리된 응답",
    example =
        """
       {
         "items": [],
         "pageInfo": {
           "currentPage": 0,
           "totalPages": 10,
           "pageSize": 12,
           "totalElements": 120,
           "first": true,
           "last": false,
           "empty": false
         }
       }
       """)
public class PageResponse<T> {

  @Schema(description = "현재 페이지의 아이템 목록")
  private List<T> items; // 현재 페이지의 아이템 목록

  @Schema(description = "페이지 정보")
  private PageInfo pageInfo; // 페이지 정보

  @Schema(description = "추가 메타데이터 (선택적)")
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
  @Schema(description = "페이지 정보")
  public static class PageInfo {
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int currentPage;

    @Schema(description = "전체 페이지 수", example = "10")
    private int totalPages;

    @Schema(description = "페이지당 항목 수", example = "12")
    private int pageSize;

    @Schema(description = "전체 항목 수", example = "120")
    private long totalElements;

    @Schema(description = "첫 페이지 여부", example = "true")
    private boolean first;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;

    @Schema(description = "결과가 비어있는지 여부", example = "false")
    private boolean empty;

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
  @Schema(description = "추가 메타데이터")
  public static class MetaData {
    @Schema(description = "검색어", example = "자바")
    private String searchTerm;

    @Schema(description = "적용된 필터", example = "ACTIVE")
    private String filter;

    @Schema(description = "정렬 기준", example = "createdAt")
    private String sortBy;

    @Schema(description = "정렬 방향", example = "DESC")
    private String sortDirection;

    @Schema(description = "카테고리 ID 목록", example = "[\"1\", \"2\", \"3\"]")
    private List<String> categoryIds;

    @Schema(description = "기간 내 총 조회수", example = "3000")
    private Long totalViews;

    @Schema(description = "마켓 이름", example = "프리미엄 마켓")
    private String marketName;

    @Schema(description = "콘텐츠 제목", example = "자바 프로그래밍 코칭")
    private String contentTitle;

    @Schema(description = "마켓 링크 존재 여부", example = "true")
    private Boolean hasMarketLink;

    @Schema(description = "마켓 링크 URL", example = "groble.shop/my-market")
    private String marketLinkUrl;
  }
}
