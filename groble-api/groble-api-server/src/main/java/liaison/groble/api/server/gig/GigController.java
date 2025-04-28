package liaison.groble.api.server.gig;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.gig.request.draft.GigDraftRequest;
import liaison.groble.api.model.gig.request.register.GigRegisterRequest;
import liaison.groble.api.model.gig.response.GigDetailResponse;
import liaison.groble.api.model.gig.response.GigPreviewCardResponse;
import liaison.groble.api.model.gig.response.GigResponse;
import liaison.groble.api.server.gig.mapper.GigDtoMapper;
import liaison.groble.application.gig.GigService;
import liaison.groble.application.gig.dto.GigCardDto;
import liaison.groble.application.gig.dto.GigDetailDto;
import liaison.groble.application.gig.dto.GigDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.request.CursorRequest;
import liaison.groble.common.response.CursorResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/gigs")
@RequiredArgsConstructor
@Tag(name = "서비스 상품 API", description = "서비스 상품 조회 및 등록(임시 저장, 심사 요청) API")
public class GigController {

  private final GigService gigService;
  private final GigDtoMapper gigDtoMapper;

  // 서비스 상품 단건 조회 (상세 조회)
  @Operation(summary = "서비스 상품 단건 조회", description = "서비스 상품을 상세 조회합니다.")
  @GetMapping("/{gigId}")
  public ResponseEntity<GrobleResponse<GigDetailResponse>> getGigDetail(
      @PathVariable("gigId") Long gigId) {

    // 서비스 상품 상세 조회 로직
    GigDetailDto gigDetailDto = gigService.getGigDetail(gigId);
    // DTO 매핑
    GigDetailResponse response = gigDtoMapper.toGigDetailResponse(gigDetailDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "서비스 상품 상세 조회 성공"));
  }

  // 서비스 상품 임시 저장
  @Operation(summary = "서비스 상품 임시 저장", description = "서비스 상품을 임시 저장합니다.")
  @ApiResponse(description = "서비스 상품을 임시 저장합니다.")
  @PostMapping("/draft")
  public ResponseEntity<GrobleResponse<GigResponse>> saveDraft(
      @Auth Accessor accessor, @Valid @RequestBody GigDraftRequest request) {
    // 1. 요청 DTO를 서비스 DTO로 변환
    GigDto gigDto = gigDtoMapper.toServiceGigDtoFromDraft(request);

    // 2. 서비스 호출 (저장 및 결과 반환 - 단일 트랜잭션)
    GigDto savedGigDto = gigService.saveDraftAndReturn(accessor.getUserId(), gigDto);

    // 3. 응답 DTO로 변환
    GigResponse response = gigDtoMapper.toGigDraftResponse(savedGigDto);

    return ResponseEntity.ok(GrobleResponse.success(response, "서비스 상품 임시 저장 성공"));
  }

  // 서비스 상품 심사 요청
  @Operation(summary = "서비스 상품 심사 요청", description = "서비스 상품을 심사 요청합니다.")
  @PostMapping("/register")
  public ResponseEntity<GrobleResponse<GigResponse>> registerGig(
      @Auth Accessor accessor, @Valid @RequestBody GigRegisterRequest request) {
    // 1. 요청 DTO를 서비스 DTO로 변환
    GigDto gigDto = gigDtoMapper.toServiceGigDtoFromRegister(request);
    GigDto savedGigDto = gigService.registerGig(accessor.getUserId(), gigDto);

    GigResponse response = gigDtoMapper.toGigDraftResponse(savedGigDto);
    return ResponseEntity.ok(GrobleResponse.success(response, "서비스 상품 심사 요청 성공"));
  }

  // 나의 코칭 상품 (판매자)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "나의 코칭 상품 조회 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
  })
  @Operation(summary = "나의 코칭 상품 조회", description = "나의 코칭 상품을 조회합니다.")
  @GetMapping("/my/coaching")
  public ResponseEntity<GrobleResponse<CursorResponse<GigPreviewCardResponse>>> getMyCoachingGigs(
      @Parameter(hidden = true) @Auth Accessor accessor,
      @Parameter(
              description = "커서 기반 페이지네이션 요청 정보",
              required = true,
              schema = @Schema(implementation = CursorRequest.class))
          @Valid
          @ModelAttribute
          CursorRequest cursorRequest,
      @Parameter(
              description = "상품 상태 필터 (DRAFT, PENDING, ACTIVE 등)",
              schema =
                  @Schema(
                      implementation = String.class,
                      allowableValues = {"DRAFT", "PENDING", "ACTIVE", "REJECTED", "SUSPENDED"}))
          @RequestParam(value = "state", required = false)
          String state) {
    // 서비스 호출
    CursorResponse<GigCardDto> cardDtos =
        gigService.getMyCoachingGigs(
            accessor.getUserId(), cursorRequest.getCursor(), cursorRequest.getSize(), state);

    // DTO 변환
    List<GigPreviewCardResponse> responseItems =
        cardDtos.getItems().stream().map(gigDtoMapper::toGigPreviewCardFromCardDto).toList();

    // CursorResponse 생성
    CursorResponse<GigPreviewCardResponse> response =
        CursorResponse.<GigPreviewCardResponse>builder()
            .items(responseItems)
            .nextCursor(cardDtos.getNextCursor())
            .hasNext(cardDtos.isHasNext())
            .totalCount(cardDtos.getTotalCount())
            .meta(cardDtos.getMeta())
            .build();

    return ResponseEntity.ok(GrobleResponse.success(response, "나의 코칭 상품 조회 성공"));
  }

  // 상품 등록
  // 상품 수정
  // 상품 삭제
  // 상품 검색
  // 상품 필터링
  // 상품 정렬
  // 상품 상세 조회
  // 상품 리뷰 조회
  // 상품 리뷰 작성
  // 상품 리뷰 수정
  // 상품 리뷰 삭제
}
