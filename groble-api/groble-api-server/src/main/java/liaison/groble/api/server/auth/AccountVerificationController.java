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
import liaison.groble.api.server.file.mapper.FileDtoMapper;
import liaison.groble.application.auth.dto.VerifyBusinessMakerAccountDto;
import liaison.groble.application.auth.dto.VerifyPersonalMakerAccountDto;
import liaison.groble.application.auth.service.AccountVerificationService;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileDto;
import liaison.groble.application.file.dto.FileUploadDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account-verification")
@Tag(name = "은행 계좌 인증 API", description = "은행 계좌 인증 관련 API (개인 메이커 인증 & 개인 • 법인 사업자)")
public class AccountVerificationController {

  private final AccountVerificationService accountVerificationService;
  private final FileService fileService;
  private final FileDtoMapper fileDtoMapper;

  /** 개인 메이커 계좌 인증 요청 처리 */
  @PersonalMaker
  @PostMapping("/personal-maker")
  public ResponseEntity<GrobleResponse<Void>> verifyPersonalMakerAccount(
      @Auth Accessor accessor, @Valid @RequestBody VerifyPersonalMakerAccountRequest request) {

    VerifyPersonalMakerAccountDto verifyPersonalMakerAccountDto =
        VerifyPersonalMakerAccountDto.builder()
            .bankAccountOwner(request.getBankAccountOwner())
            .bankName(request.getBankName())
            .bankAccountNumber(request.getBankAccountNumber())
            .copyOfBankbookUrl(request.getCopyOfBankbookUrl())
            .build();

    accountVerificationService.verifyPersonalMakerAccount(
        accessor.getUserId(), verifyPersonalMakerAccountDto);
    return ResponseEntity.ok(GrobleResponse.success(null));
  }

  @BusinessMaker
  @PostMapping("/business-maker")
  public ResponseEntity<GrobleResponse<Void>> verifyBusinessBankbook(
      @Auth Accessor accessor,
      @Valid @RequestBody VerificationBusinessMakerAccountRequest request) {

    VerifyBusinessMakerAccountDto verifyBusinessMakerAccountDto =
        VerifyBusinessMakerAccountDto.builder()
            .bankAccountOwner(request.getBankAccountOwner())
            .bankName(request.getBankName())
            .bankAccountNumber(request.getBankAccountNumber())
            .copyOfBankbookUrl(request.getCopyOfBankbookUrl())
            .build();

    accountVerificationService.verifyBusinessBankbook(
        accessor.getUserId(), verifyBusinessMakerAccountDto);
    return ResponseEntity.ok(GrobleResponse.success(null));
  }

  /** 개인 • 법인 사업자 계좌 인증 요청 처리 */
  @BusinessVerification
  @PostMapping("/business")
  public ResponseEntity<GrobleResponse<Void>> verifyBusinessAccount(
      @Auth Accessor accessor,
      @Valid @RequestBody VerificationBusinessMakerAccountRequest request) {

    VerifyBusinessMakerAccountDto verifyBusinessMakerAccountDto =
        VerifyBusinessMakerAccountDto.builder()
            .businessType(convertToBusinessTypeDto(request.getBusinessType()))
            .businessCategory(request.getBusinessCategory())
            .businessSector(request.getBusinessSector())
            .businessName(request.getBusinessName())
            .representativeName(request.getRepresentativeName())
            .businessAddress(request.getBusinessAddress())
            .businessLicenseFileUrl(request.getBusinessLicenseFileUrl())
            .taxInvoiceEmail(request.getTaxInvoiceEmail())
            .build();

    accountVerificationService.verifyBusinessAccount(
        accessor.getUserId(), verifyBusinessMakerAccountDto);
    return ResponseEntity.ok(GrobleResponse.success(null));
  }

  /** 통장 사본 첨부 파일 업로드 */
  @PostMapping("/upload-bankbook-copy")
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
      FileUploadDto fileUploadDto =
          fileDtoMapper.toServiceFileUploadDto(bankbookCopyImage, "bankbook");
      FileDto fileDto = fileService.uploadFile(accessor.getUserId(), fileUploadDto);
      FileUploadResponse response =
          FileUploadResponse.of(
              fileDto.getOriginalFilename(),
              fileDto.getFileUrl(),
              fileDto.getContentType(),
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
  @PostMapping("/upload-business-license")
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
      FileUploadDto fileUploadDto =
          fileDtoMapper.toServiceFileUploadDto(businessLicenseImage, "business/license");
      FileDto fileDto = fileService.uploadFile(accessor.getUserId(), fileUploadDto);
      FileUploadResponse response =
          FileUploadResponse.of(
              fileDto.getOriginalFilename(),
              fileDto.getFileUrl(),
              fileDto.getContentType(),
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

  private VerifyBusinessMakerAccountDto.BusinessType convertToBusinessTypeDto(
      VerificationBusinessMakerAccountRequest.BusinessType businessType) {

    // 예: request.getBusinessType()이 String 또는 Enum이라고 가정
    return VerifyBusinessMakerAccountDto.BusinessType.valueOf(businessType.name());
  }
}
