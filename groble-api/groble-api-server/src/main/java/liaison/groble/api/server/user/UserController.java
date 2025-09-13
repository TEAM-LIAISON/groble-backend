package liaison.groble.api.server.user;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
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
import liaison.groble.api.model.user.response.swagger.SwitchRole;
import liaison.groble.api.model.user.response.swagger.UploadUserProfileImage;
import liaison.groble.api.model.user.response.swagger.UserHeader;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.util.FileUtil;
import liaison.groble.api.server.util.FileValidationUtil;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileUploadDTO;
import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.application.user.dto.UserMyPageDetailDTO;
import liaison.groble.application.user.dto.UserMyPageSummaryDTO;
import liaison.groble.application.user.service.UserService;
import liaison.groble.application.user.strategy.UserHeaderProcessorFactory;
import liaison.groble.application.user.strategy.UserHeaderStrategy;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.factory.UserContextFactory;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.TokenCookieService;
import liaison.groble.mapping.user.UserMapper;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 사용자 정보 관련 API 컨트롤러 */
@RestController
@RequestMapping
@Tag(
    name = "[👨‍💻 마이페이지] 마이페이지에서 사용하는 기능 관련 API",
    description = "마이페이지 조회, 프로필 이미지 업로드, 가입 유형 전환을 진행합니다.")
public class UserController extends BaseController {

  // 응답 메시지 상수화
  private static final String USER_SWITCH_ROLE_SUCCESS_MESSAGE = "가입 유형이 전환되었습니다.";
  private static final String USER_MY_PAGE_SUMMARY_SUCCESS_MESSAGE = "마이페이지 요약 정보 조회에 성공했습니다.";
  private static final String USER_MY_PAGE_DETAIL_SUCCESS_MESSAGE = "마이페이지 상세 정보 조회에 성공했습니다.";

  // Factory
  private final UserHeaderProcessorFactory userHeaderProcessorFactory;

  // Service
  private final UserService userService;
  private final FileService fileService;
  private final TokenCookieService tokenCookieService;

  // Mapper
  private final UserMapper userMapper;

  // Utils
  private final FileUtil fileUtil;
  private final FileValidationUtil fileValidationUtil;

  public UserController(
      ResponseHelper responseHelper,
      UserHeaderProcessorFactory userHeaderProcessorFactory,
      UserService userService,
      FileService fileService,
      TokenCookieService tokenCookieService,
      UserMapper userMapper,
      FileUtil fileUtil,
      FileValidationUtil fileValidationUtil) {
    super(responseHelper);
    this.userHeaderProcessorFactory = userHeaderProcessorFactory;
    this.userService = userService;
    this.fileService = fileService;
    this.tokenCookieService = tokenCookieService;
    this.userMapper = userMapper;
    this.fileUtil = fileUtil;
    this.fileValidationUtil = fileValidationUtil;
  }

  @SwitchRole
  @PostMapping(ApiPaths.User.SWITCH_ROLE)
  public ResponseEntity<GrobleResponse<Void>> switchUserType(
      @Auth Accessor accessor, @Valid @RequestBody UserTypeRequest request) {

    boolean success = userService.switchUserType(accessor.getUserId(), request.getUserType());

    if (success) {
      return responseHelper.success(null, USER_SWITCH_ROLE_SUCCESS_MESSAGE, HttpStatus.NO_CONTENT);
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
  @GetMapping(ApiPaths.User.MY_PAGE_SUMMARY)
  public ResponseEntity<GrobleResponse<MyPageSummaryResponseBase>> getUserMyPageSummary(
      @Auth Accessor accessor) {
    UserMyPageSummaryDTO userMyPageSummaryDTO =
        userService.getUserMyPageSummary(accessor.getUserId());
    return ResponseEntity.ok(
        GrobleResponse.success(userMapper.toApiMyPageSummaryResponse(userMyPageSummaryDTO)));
  }

  /** 마이페이지 상세 정보 조회 */
  @MyPageDetail
  @GetMapping(ApiPaths.User.MY_PAGE_DETAIL)
  public ResponseEntity<GrobleResponse<UserMyPageDetailResponse>> getUserMyPageDetail(
      @Auth Accessor accessor) {
    UserMyPageDetailDTO detailDTO = userService.getUserMyPageDetail(accessor.getUserId());
    return ResponseEntity.ok(
        GrobleResponse.success(userMapper.toApiMyPageDetailResponse(detailDTO)));
  }

  @UserHeader
  @GetMapping(ApiPaths.User.MY_PAGE)
  @Logging(item = "User", action = "getUserHeaderInform", includeParam = true, includeResult = true)
  public ResponseEntity<GrobleResponse<UserHeaderResponse>> getUserHeaderInform(
      @Auth(required = false) Accessor accessor, HttpServletResponse httpResponse) {

    UserContext userContext = UserContextFactory.from(accessor);
    UserHeaderStrategy processor = userHeaderProcessorFactory.getProcessor(userContext);

    UserHeaderDTO userHeaderDTO = processor.processUserHeader(userContext, httpResponse);
    UserHeaderResponse response = userMapper.toUserHeaderResponse(userHeaderDTO);
    return success(response, ResponseMessages.User.USER_HEADER_INFORM_SUCCESS);
  }

  /** 사용자 프로필 이미지 업로드 */
  // 프로필 이미지 추가 업로드 및 수정 진행
  @UploadUserProfileImage
  @PostMapping(
      value = ApiPaths.User.UPLOAD_PROFILE_IMAGE,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<GrobleResponse<?>> uploadProfileImage(
      @Auth Accessor accessor,
      @RequestPart("profileImage")
          @Parameter(
              description = "프로필 이미지 파일",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          MultipartFile profileImage) {

    // 파일 검증 (5MB 제한)
    FileValidationUtil.FileValidationResult validationResult =
        fileValidationUtil.validateFile(profileImage, FileValidationUtil.FileType.STRICT_IMAGE, 5);

    if (!validationResult.isValid()) {
      return ResponseEntity.badRequest()
          .body(
              GrobleResponse.error(
                  validationResult.getErrorMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    try {
      // 3) DTO 변환
      FileUploadDTO dto =
          fileUtil.toServiceFileUploadDTO(profileImage, "profiles/" + accessor.getUserId());
      // 4) 업로드
      var fileDTO = fileService.uploadFile(accessor.getUserId(), dto);
      // 5) 사용자 프로필에 URL 업데이트
      userService.updateProfileImageUrl(accessor.getUserId(), fileDTO.getFileUrl());

      // 6) 응답 생성
      FileUploadResponse response =
          FileUploadResponse.of(
              fileDTO.getOriginalFilename(),
              fileDTO.getFileUrl(),
              fileDTO.getContentType(),
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
}
