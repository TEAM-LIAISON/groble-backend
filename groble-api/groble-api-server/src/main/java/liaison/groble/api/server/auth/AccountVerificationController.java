package liaison.groble.api.server.auth;

import java.io.IOException;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.api.model.auth.request.VerificationBusinessMakerAccountRequest;
import liaison.groble.api.model.auth.request.VerifyPersonalMakerAccountRequest;
import liaison.groble.api.model.auth.response.swagger.BusinessMaker;
import liaison.groble.api.model.auth.response.swagger.BusinessVerification;
import liaison.groble.api.model.auth.response.swagger.PersonalMaker;
import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.api.server.util.FileUtils;
import liaison.groble.application.auth.dto.VerifyBusinessMakerAccountDTO;
import liaison.groble.application.auth.dto.VerifyPersonalMakerAccountDTO;
import liaison.groble.application.auth.service.AccountVerificationService;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileDTO;
import liaison.groble.application.file.dto.FileUploadDTO;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.auth.AccountVerificationMapper;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account-verification")
@Tag(name = "[🏧 계좌]은행 계좌 인증 API", description = "은행 계좌 인증 관련 API (개인 메이커 인증 & 개인 • 법인 사업자)")
public class AccountVerificationController {

  // API 경로 상수화
  private static final String PERSONAL_MAKER_VERIFICATION_PATH = "/personal-maker";
  private static final String BUSINESS_MAKER_VERIFICATION_PATH = "/business-maker";
  private static final String BUSINESS_VERIFICATION_PATH = "/business";
  private static final String UPLOAD_BANKBOOK_COPY_PATH = "/upload-bankbook-copy";
  private static final String UPLOAD_BUSINESS_LICENSE_PATH = "/upload-business-license";

  // 응답 메시지 상수화
  private static final String PERSONAL_MAKER_VERIFICATION_SUCCESS_MESSAGE =
      "개인 메이커 계좌 인증 요청이 성공적으로 처리되었습니다.";
  private static final String BUSINESS_MAKER_VERIFICATION_SUCCESS_MESSAGE =
      "법인 사업자 계좌 인증 요청이 성공적으로 처리되었습니다.";
  private static final String BUSINESS_VERIFICATION_SUCCESS_MESSAGE =
      "개인 • 법인 사업자 계좌 인증 요청이 성공적으로 처리되었습니다.";
  private static final String UPLOAD_BANKBOOK_COPY_SUCCESS_MESSAGE =
      "통장 사본 이미지 업로드가 성공적으로 완료되었습니다.";
  private static final String UPLOAD_BUSINESS_LICENSE_SUCCESS_MESSAGE =
      "사업자 등록증 사본 이미지 업로드가 성공적으로 완료되었습니다.";

  // Service
  private final AccountVerificationService accountVerificationService;
  private final FileService fileService;
  private final FileUtils fileUtils;

  // Mapper
  private final AccountVerificationMapper accountVerificationMapper;
  // Helper
  private final ResponseHelper responseHelper;

  /** 개인 메이커 계좌 인증 요청 처리 */
  @PersonalMaker
  @PostMapping(PERSONAL_MAKER_VERIFICATION_PATH)
  public ResponseEntity<GrobleResponse<Void>> verifyPersonalMakerAccount(
      @Auth Accessor accessor, @Valid @RequestBody VerifyPersonalMakerAccountRequest request) {

    VerifyPersonalMakerAccountDTO verifyPersonalMakerAccountDTO =
        accountVerificationMapper.toVerifyPersonalMakerAccountDTO(request);
    accountVerificationService.verifyPersonalMakerAccount(
        accessor.getUserId(), verifyPersonalMakerAccountDTO);
    return responseHelper.success(null, PERSONAL_MAKER_VERIFICATION_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @BusinessMaker
  @PostMapping(BUSINESS_MAKER_VERIFICATION_PATH)
  public ResponseEntity<GrobleResponse<Void>> verifyBusinessBankbook(
      @Auth Accessor accessor,
      @Valid @RequestBody VerificationBusinessMakerAccountRequest request) {

    VerifyBusinessMakerAccountDTO verifyBusinessMakerAccountDTO =
        accountVerificationMapper.toVerifyBusinessMakerAccountDTO(request);

    accountVerificationService.verifyBusinessBankbook(
        accessor.getUserId(), verifyBusinessMakerAccountDTO);
    return responseHelper.success(null, BUSINESS_MAKER_VERIFICATION_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  /** 개인 • 법인 사업자 계좌 인증 요청 처리 */
  @BusinessVerification
  @PostMapping(BUSINESS_VERIFICATION_PATH)
  public ResponseEntity<GrobleResponse<Void>> verifyBusinessAccount(
      @Auth Accessor accessor,
      @Valid @RequestBody VerificationBusinessMakerAccountRequest request) {

    VerifyBusinessMakerAccountDTO verifyBusinessMakerAccountDTO =
        accountVerificationMapper.toVerifyBusinessMakerAccountDTO(request);

    accountVerificationService.verifyBusinessAccount(
        accessor.getUserId(), verifyBusinessMakerAccountDTO);
    return ResponseEntity.ok(GrobleResponse.success(null));
  }

  /** 통장 사본 첨부 파일 업로드 */
  @PostMapping(UPLOAD_BANKBOOK_COPY_PATH)
  public ResponseEntity<GrobleResponse<?>> uploadBankbookCopyImage(
      @Auth final Accessor accessor,
      @RequestPart("bankbookCopyImage")
          @Parameter(
              description = "통장 사본 이미지 파일",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          @Valid
          final MultipartFile bankbookCopyImage) {

    if (bankbookCopyImage == null || bankbookCopyImage.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("pdf, png, jpeg 파일을 선택해주세요.", HttpStatus.BAD_REQUEST.value()));
    }

    if (!isAllowedFileType(bankbookCopyImage)) {
      return ResponseEntity.badRequest()
          .body(
              GrobleResponse.error(
                  "pdf, png, jpeg 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST.value()));
    }

    try {
      FileUploadDTO fileUploadDTO = fileUtils.toServiceFileUploadDTO(bankbookCopyImage, "bankbook");
      FileDTO fileDTO = fileService.uploadFile(accessor.getUserId(), fileUploadDTO);
      FileUploadResponse response =
          FileUploadResponse.of(
              fileDTO.getOriginalFilename(),
              fileDTO.getFileUrl(),
              fileDTO.getContentType(),
              "bankbook");
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              GrobleResponse.success(
                  response, "통장 사본 이미지 업로드가 성공적으로 완료되었습니다.", HttpStatus.CREATED.value()));
    } catch (IOException ioe) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              GrobleResponse.error(
                  "통장 사본 저장 중 오류가 발생했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /** 통장 사본 첨부 파일 업로드 */
  @PostMapping(UPLOAD_BUSINESS_LICENSE_PATH)
  public ResponseEntity<GrobleResponse<?>> uploadBusinessLicenseImage(
      @Auth final Accessor accessor,
      @RequestPart("businessLicenseImage")
          @Parameter(
              description = "사업자 등록증 사본 이미지 파일",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          @Valid
          final MultipartFile businessLicenseImage) {

    if (businessLicenseImage == null || businessLicenseImage.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("pdf, png, jpeg 파일을 선택해주세요.", HttpStatus.BAD_REQUEST.value()));
    }

    if (!isAllowedFileType(businessLicenseImage)) {
      return ResponseEntity.badRequest()
          .body(
              GrobleResponse.error(
                  "pdf, png, jpeg 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST.value()));
    }

    try {
      FileUploadDTO fileUploadDTO =
          fileUtils.toServiceFileUploadDTO(businessLicenseImage, "business/license");
      FileDTO fileDTO = fileService.uploadFile(accessor.getUserId(), fileUploadDTO);
      FileUploadResponse response =
          FileUploadResponse.of(
              fileDTO.getOriginalFilename(),
              fileDTO.getFileUrl(),
              fileDTO.getContentType(),
              "business/license");
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              GrobleResponse.success(
                  response, "사업자 등록증 사본 이미지 업로드가 성공적으로 완료되었습니다.", HttpStatus.CREATED.value()));
    } catch (IOException ioe) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              GrobleResponse.error(
                  "사업자 등록증 사본 저장 중 오류가 발생했습니다. 다시 시도해주세요.",
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  private boolean isAllowedFileType(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null
        && (contentType.equalsIgnoreCase("application/pdf")
            || contentType.equalsIgnoreCase("image/jpeg")
            || contentType.equalsIgnoreCase("image/png"));
  }
}
