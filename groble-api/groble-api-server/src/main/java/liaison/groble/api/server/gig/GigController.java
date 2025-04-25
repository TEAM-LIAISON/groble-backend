package liaison.groble.api.server.gig;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.gig.request.GigDraftRequest;
import liaison.groble.api.model.gig.request.GigRegisterRequest;
import liaison.groble.api.model.gig.response.GigDraftResponse;
import liaison.groble.api.server.gig.mapper.GigDtoMapper;
import liaison.groble.application.gig.GigService;
import liaison.groble.application.gig.dto.GigDraftDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/gigs")
@RequiredArgsConstructor
@Tag(name = "서비스 상품 API")
public class GigController {

  private final GigService gigService;
  private final GigDtoMapper gigDtoMapper;

  // 서비스 상품 단건 조회
  @Operation(summary = "서비스 상품 단건 조회", description = "서비스 상품을 상세 조회합니다.")
  @GetMapping("/{gigId}")
  public ResponseEntity<GrobleResponse<Void>> getGigDetail(@PathVariable("gigId") Long gigId) {

    return ResponseEntity.ok(GrobleResponse.success(null, "서비스 상품 상세 조회 성공"));
  }

  // 서비스 상품 임시 저장
  @Operation(summary = "서비스 상품 임시 저장", description = "서비스 상품을 임시 저장합니다.")
  @PostMapping("/draft")
  public ResponseEntity<GrobleResponse<GigDraftResponse>> saveDraft(
      @Auth Accessor accessor, @RequestBody GigDraftRequest request) {
    GigDraftDto gigDraftDto = gigDtoMapper.toServiceGigDraftDto(request);
    gigService.saveDraft(accessor.getUserId(), gigDraftDto);

    return ResponseEntity.ok(GrobleResponse.success(null, "서비스 상품 임시 저장 성공"));
  }

  // 서비스 상품 심사 요청
  @Operation(summary = "서비스 상품 심사 요청", description = "서비스 상품을 심사 요청합니다.")
  @PostMapping("/register")
  public ResponseEntity<GrobleResponse<Void>> registerGig(
      @Auth Accessor accessor, @RequestBody GigRegisterRequest request) {
    return ResponseEntity.ok(GrobleResponse.success(null, "서비스 상품 심사 요청 성공"));
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
