package liaison.groble.api.server.user;

import java.io.IOException;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.api.model.user.request.UserTypeRequest;
import liaison.groble.api.model.user.response.MyPageSummaryResponseBase;
import liaison.groble.api.model.user.response.UserHeaderResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.api.model.user.response.swagger.MyPageDetail;
import liaison.groble.api.model.user.response.swagger.MyPageSummary;
import liaison.groble.api.model.user.response.swagger.SwitchRole;
import liaison.groble.api.model.user.response.swagger.UploadUserProfileImage;
import liaison.groble.api.model.user.response.swagger.UserHeader;
import liaison.groble.api.server.file.mapper.FileDtoMapper;
import liaison.groble.api.server.user.mapper.UserDtoMapper;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileUploadDto;
import liaison.groble.application.user.dto.UserHeaderDto;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** 사용자 정보 관련 API 컨트롤러 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "사용자 정보 관련 API", description = "마이페이지 조회 및 가입 유형 전환 API")
public class UserController {

  private final UserService userService;
  private final UserDtoMapper userDtoMapper;
  private final FileService fileService;
  private final FileDtoMapper fileDtoMapper;

  @SwitchRole
  @PostMapping("/users/switch-role")
  public ResponseEntity<GrobleResponse<Void>> switchUserType(
      @Auth Accessor accessor, @Valid @RequestBody UserTypeRequest request) {

    boolean success = userService.switchUserType(accessor.getUserId(), request.getUserType());

    if (success) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT)
          .body(GrobleResponse.success(null, "가입 유형이 전환되었습니다.", 204));
    } else {
      String target = request.getUserType().toUpperCase();
      String message;
      if ("BUYER".equals(target)) {
        // 판매자 → 구매자 전환 실패
        message = "구매자 전환을 진행할 수 없습니다.";
      } else if ("SELLER".equals(target)) {
        // 구매자 → 판매자 전환 실패
        message = "판매자 인증이 필요합니다.";
      } else {
        message = "유효하지 않은 전환 요청입니다.";
      }
      return ResponseEntity.badRequest()
          .body(GrobleResponse.fail(message, HttpStatus.BAD_REQUEST.value()));
    }
  }

  /** 마이페이지 요약 정보 조회 */
  @MyPageSummary
  @GetMapping("/users/me/summary")
  public ResponseEntity<GrobleResponse<MyPageSummaryResponseBase>> getUserMyPageSummary(
      @Auth Accessor accessor) {
    UserMyPageSummaryDto summaryDto = userService.getUserMyPageSummary(accessor.getUserId());
    return ResponseEntity.ok(
        GrobleResponse.success(userDtoMapper.toApiMyPageSummaryResponse(summaryDto)));
  }

  /** 마이페이지 상세 정보 조회 */
  @MyPageDetail
  @GetMapping("/users/me/detail")
  public ResponseEntity<GrobleResponse<UserMyPageDetailResponse>> getUserMyPageDetail(
      @Auth Accessor accessor) {
    UserMyPageDetailDto detailDto = userService.getUserMyPageDetail(accessor.getUserId());
    return ResponseEntity.ok(
        GrobleResponse.success(userDtoMapper.toApiMyPageDetailResponse(detailDto)));
  }

  // 홈화면 헤더 정보 조회
  @UserHeader
  @GetMapping("/me")
  public ResponseEntity<GrobleResponse<UserHeaderResponse>> getUserHeaderInform(
      @Auth Accessor accessor) {

    // 로그인하지 않은 경우
    if (accessor == null) {
      UserHeaderResponse response =
          UserHeaderResponse.builder()
              .isLogin(false)
              .nickname(null)
              .profileImageUrl(null)
              .canSwitchToSeller(false)
              .unreadNotificationCount(0)
              .alreadyRegisteredAsSeller(false)
              .lastUserType(null)
              .build();

      return ResponseEntity.ok(GrobleResponse.success(response, "사용자 정보 조회 성공"));
    }

    // 로그인한 경우 - 기존 코드 활용
    UserHeaderDto userHeaderDto = userService.getUserHeaderInform(accessor.getUserId());
    UserHeaderResponse response = userDtoMapper.toApiUserHeaderResponse(userHeaderDto);

    return ResponseEntity.ok(GrobleResponse.success(response, "사용자 헤더 정보 조회 성공"));
  }

  /** 사용자 프로필 이미지 업로드 */
  // 프로필 이미지 추가 업로드 및 수정 진행
  @UploadUserProfileImage
  @PostMapping(value = "/users/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<GrobleResponse<?>> uploadProfileImage(
      @Auth Accessor accessor,
      @RequestPart("profileImage")
          @Parameter(
              description = "프로필 이미지 파일",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          MultipartFile profileImage) {

    // 1) 파일 미선택
    if (profileImage == null || profileImage.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("이미지 파일을 선택해주세요.", HttpStatus.BAD_REQUEST.value()));
    }
    // 2) 이미지 타입 검증
    if (!isImageFile(profileImage)) {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("이미지 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST.value()));
    }

    try {
      // 3) DTO 변환
      FileUploadDto dto =
          fileDtoMapper.toServiceFileUploadDto(profileImage, "profiles/" + accessor.getUserId());
      // 4) 업로드
      var fileDto = fileService.uploadFile(accessor.getUserId(), dto);
      // 5) 사용자 프로필에 URL 업데이트
      userService.updateProfileImageUrl(accessor.getUserId(), fileDto.getFileUrl());

      // 6) 응답 생성
      FileUploadResponse response =
          FileUploadResponse.of(
              fileDto.getOriginalFilename(),
              fileDto.getFileUrl(),
              fileDto.getContentType(),
              dto.getDirectory());
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              GrobleResponse.success(
                  response, "프로필 이미지가 성공적으로 업로드되었습니다.", HttpStatus.CREATED.value()));

    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              GrobleResponse.error(
                  "프로필 이미지 저장 중 오류가 발생했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  private boolean isImageFile(MultipartFile file) {
    String ct = file.getContentType();
    return ct != null && ct.startsWith("image/");
  }
}
