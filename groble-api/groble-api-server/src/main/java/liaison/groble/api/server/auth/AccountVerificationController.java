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
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.api.model.auth.request.VerificationBusinessMakerAccountRequest;
import liaison.groble.api.model.auth.request.VerifyPersonalMakerAccountRequest;
import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.api.server.auth.docs.AccountVerificationApiResponses;
import liaison.groble.api.server.auth.docs.AccountVerificationSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.util.FileUtil;
import liaison.groble.api.server.util.FileValidationUtil;
import liaison.groble.application.auth.dto.VerifyBusinessMakerAccountDTO;
import liaison.groble.application.auth.dto.VerifyPersonalMakerAccountDTO;
import liaison.groble.application.auth.service.AccountVerificationService;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileDTO;
import liaison.groble.application.file.dto.FileUploadDTO;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.auth.AccountVerificationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Auth.ACCOUNT_VERIFICATION)
@Tag(
    name = AccountVerificationSwaggerDocs.TAG_NAME,
    description = AccountVerificationSwaggerDocs.TAG_DESCRIPTION)
public class AccountVerificationController extends BaseController {

  // Service
  private final AccountVerificationService accountVerificationService;
  private final FileService fileService;
  private final FileUtil fileUtil;
  private final FileValidationUtil fileValidationUtil;

  // Mapper
  private final AccountVerificationMapper accountVerificationMapper;

  public AccountVerificationController(
      ResponseHelper responseHelper,
      AccountVerificationMapper accountVerificationMapper,
      AccountVerificationService accountVerificationService,
      FileService fileService,
      FileUtil fileUtil,
      FileValidationUtil fileValidationUtil) {
    super(responseHelper);
    this.accountVerificationMapper = accountVerificationMapper;
    this.accountVerificationService = accountVerificationService;
    this.fileService = fileService;
    this.fileUtil = fileUtil;
    this.fileValidationUtil = fileValidationUtil;
  }

  /** 개인 메이커 계좌 인증 요청 처리 */
  @Operation(
      summary = AccountVerificationSwaggerDocs.PERSONAL_MAKER_VERIFICATION_SUMMARY,
      description = AccountVerificationSwaggerDocs.PERSONAL_MAKER_VERIFICATION_DESCRIPTION)
  @Logging(
      item = "AccountVerification",
      action = "verifyPersonalMakerAccount",
      includeParam = true,
      includeResult = true)
  @AccountVerificationApiResponses.PersonalMakerAccountVerificationResponses
  @PostMapping(ApiPaths.Auth.PERSONAL_MAKER_VERIFICATION)
  public ResponseEntity<GrobleResponse<Void>> verifyPersonalMakerAccount(
      @Auth Accessor accessor, @Valid @RequestBody VerifyPersonalMakerAccountRequest request) {

    VerifyPersonalMakerAccountDTO verifyPersonalMakerAccountDTO =
        accountVerificationMapper.toVerifyPersonalMakerAccountDTO(request);
    accountVerificationService.verifyPersonalMakerAccount(
        accessor.getUserId(), verifyPersonalMakerAccountDTO);
    return success(null, ResponseMessages.Auth.PERSONAL_MAKER_VERIFICATION_SUCCESS);
  }

  /** 개인 • 법인 사업자 계좌, 통장 인증 요청 처리 */
  @Operation(
      summary = AccountVerificationSwaggerDocs.BUSINESS_MAKER_BANKBOOK_VERIFICATION_SUMMARY,
      description = AccountVerificationSwaggerDocs.BUSINESS_MAKER_BANKBOOK_VERIFICATION_DESCRIPTION)
  @Logging(
      item = "AccountVerification",
      action = "verifyBusinessBankbook",
      includeParam = true,
      includeResult = true)
  @AccountVerificationApiResponses.BusinessBankbookVerificationResponses
  @PostMapping(ApiPaths.Auth.BUSINESS_MAKER_BANKBOOK_VERIFICATION)
  public ResponseEntity<GrobleResponse<Void>> verifyBusinessBankbook(
      @Auth Accessor accessor,
      @Valid @RequestBody VerificationBusinessMakerAccountRequest request) {

    VerifyBusinessMakerAccountDTO verifyBusinessMakerAccountDTO =
        accountVerificationMapper.toVerifyBusinessMakerLicenseDTO(request);

    accountVerificationService.verifyBusinessBankbook(
        accessor.getUserId(), verifyBusinessMakerAccountDTO);
    return success(null, ResponseMessages.Auth.BUSINESS_MAKER_BANKBOOK_VERIFICATION_SUCCESS);
  }

  /** 개인 • 법인 사업자 사업자 등록증 인증 요청 처리 */
  @Operation(
      summary = AccountVerificationSwaggerDocs.BUSINESS_MAKER_VERIFICATION_SUMMARY,
      description = AccountVerificationSwaggerDocs.BUSINESS_MAKER_VERIFICATION_DESCRIPTION)
  @Logging(
      item = "AccountVerification",
      action = "verifyBusinessAccount",
      includeParam = true,
      includeResult = true)
  @AccountVerificationApiResponses.BusinessLicenseVerificationResponses
  @PostMapping(ApiPaths.Auth.BUSINESS_MAKER_VERIFICATION)
  public ResponseEntity<GrobleResponse<Void>> verifyBusinessLicense(
      @Auth Accessor accessor,
      @Valid @RequestBody VerificationBusinessMakerAccountRequest request) {

    VerifyBusinessMakerAccountDTO verifyBusinessMakerAccountDTO =
        accountVerificationMapper.toVerifyBusinessMakerLicenseDTO(request);

    accountVerificationService.verifyBusinessAccount(
        accessor.getUserId(), verifyBusinessMakerAccountDTO);
    return success(null, ResponseMessages.Auth.BUSINESS_MAKER_VERIFICATION_SUCCESS);
  }

  /** 통장 사본 첨부 파일 업로드 */
  @PostMapping(ApiPaths.Auth.UPLOAD_BANKBOOK_COPY)
  public ResponseEntity<GrobleResponse<?>> uploadBankbookCopyImage(
      @Auth final Accessor accessor,
      @RequestPart("bankbookCopyImage")
          @Parameter(
              description = "통장 사본 이미지 파일",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          @Valid
          final MultipartFile bankbookCopyImage) {

    FileValidationUtil.FileValidationResult validationResult =
        fileValidationUtil.validateFile(
            bankbookCopyImage, FileValidationUtil.FileType.DOCUMENT, 10);

    if (!validationResult.isValid()) {
      return ResponseEntity.badRequest()
          .body(
              GrobleResponse.error(
                  validationResult.getErrorMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    try {
      FileUploadDTO fileUploadDTO = fileUtil.toServiceFileUploadDTO(bankbookCopyImage, "bankbook");
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
    } catch (MultipartException e) {
      log.error("Multipart 파싱 오류: ", e);
      return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
          .body(
              GrobleResponse.error(
                  "파일 업로드 중 연결이 끊어졌습니다. 다시 시도해주세요.", HttpStatus.REQUEST_TIMEOUT.value()));
    } catch (IOException ioe) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              GrobleResponse.error(
                  "통장 사본 저장 중 오류가 발생했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /** 통장 사본 첨부 파일 업로드 */
  @PostMapping(ApiPaths.Auth.UPLOAD_BUSINESS_LICENSE)
  public ResponseEntity<GrobleResponse<?>> uploadBusinessLicenseImage(
      @Auth final Accessor accessor,
      @RequestPart("businessLicenseImage")
          @Parameter(
              description = "사업자 등록증 사본 이미지 파일",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          @Valid
          final MultipartFile businessLicenseImage) {

    FileValidationUtil.FileValidationResult validationResult =
        fileValidationUtil.validateFile(
            businessLicenseImage, FileValidationUtil.FileType.DOCUMENT, 10);

    if (!validationResult.isValid()) {
      return ResponseEntity.badRequest()
          .body(
              GrobleResponse.error(
                  validationResult.getErrorMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    try {
      FileUploadDTO fileUploadDTO =
          fileUtil.toServiceFileUploadDTO(businessLicenseImage, "business/license");
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
    } catch (MultipartException e) {
      log.error("Multipart 파싱 오류: ", e);
      return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
          .body(
              GrobleResponse.error(
                  "파일 업로드 중 연결이 끊어졌습니다. 다시 시도해주세요.", HttpStatus.REQUEST_TIMEOUT.value()));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              GrobleResponse.error(
                  "파일 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }
}
