package liaison.groble.api.server.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.api.server.file.mapper.FileDtoMapper;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileDto;
import liaison.groble.application.file.dto.FileUploadDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.exception.FileProcessingException;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "이미지 및 첨부 파일 API", description = "파일 업로드 및 관리 API")
public class FileController {
  private final FileService fileService;
  private final FileDtoMapper fileDtoMapper;

  private static final String DEFAULT_DIRECTORY = "uploads";
  private static final String IMAGE_DIRECTORY = "images";
  private static final String DOCUMENT_DIRECTORY = "documents";
  private static final String CONTENT_DIRECTORY = "contents";

  /** 기본 파일 업로드 - 모든 파일 타입 처리 */
  @Operation(
      summary = "파일 업로드",
      description =
          "폼 데이터를 통해 다양한 유형의 파일을 업로드합니다. fileType 파라미터를 통해 파일 저장 위치를 자동으로 결정하거나 directory 파라미터로 직접 지정할 수 있습니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "파일 업로드 성공",
        content = @Content(schema = @Schema(implementation = FileUploadResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 - 파일이 없거나 손상된 경우"),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "413", description = "파일 크기 제한 초과"),
    @ApiResponse(responseCode = "500", description = "서버 오류 - 파일 저장 실패")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<GrobleResponse<FileUploadResponse>> uploadFile(
      @Auth Accessor accessor,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "fileType", required = false) String fileType,
      @RequestParam(value = "directory", required = false) String directory) {

    try {
      // 파일 타입에 따른 기본 디렉토리 결정
      String targetDirectory = directory != null ? directory : getDirectoryByFileType(fileType);

      FileUploadDto fileUploadDto = fileDtoMapper.toServiceFileUploadDto(file, targetDirectory);
      FileDto fileDto = fileService.uploadFile(accessor.getUserId(), fileUploadDto);

      FileUploadResponse response =
          FileUploadResponse.of(
              fileDto.getOriginalFilename(),
              fileDto.getFileUrl(),
              fileDto.getContentType(),
              targetDirectory);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(GrobleResponse.success(response, "파일 업로드가 성공적으로 완료되었습니다.", 201));
    } catch (IOException e) {
      log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
      throw new FileProcessingException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  /** 컨텐츠 대표 이미지 업로드 */
  @Operation(
      summary = "컨텐츠 대표 이미지 업로드",
      description = "컨텐츠 대표 이미지를 업로드합니다. 이미지 파일만 업로드 가능하며, 다른 파일 형식은 오류가 발생합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "이미지 업로드 성공",
        content = @Content(schema = @Schema(implementation = FileUploadResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 - 파일이 없거나 이미지 파일이 아닌 경우"),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "413", description = "파일 크기 제한 초과"),
    @ApiResponse(responseCode = "500", description = "서버 오류 - 파일 저장 실패")
  })
  @PostMapping(value = "/content/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<GrobleResponse<FileUploadResponse>> uploadContentThumbnail(
      @Auth Accessor accessor,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "directory", defaultValue = "contents") String directory) {

    try {
      // 이미지 파일 타입 체크
      if (!isImageFile(file)) {
        throw new FileProcessingException("이미지 파일만 업로드 가능합니다.");
      }

      FileUploadDto fileUploadDto = fileDtoMapper.toServiceFileUploadDto(file, directory);
      FileDto fileDto = fileService.uploadFile(accessor.getUserId(), fileUploadDto);

      FileUploadResponse response =
          FileUploadResponse.of(
              fileDto.getOriginalFilename(),
              fileDto.getFileUrl(),
              fileDto.getContentType(),
              directory);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(GrobleResponse.success(response, "파일 업로드가 성공적으로 완료되었습니다.", 201));
    } catch (IOException e) {
      log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
      throw new FileProcessingException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  /** 여러 파일 업로드 */
  @Operation(
      summary = "여러 컨텐츠 파일 업로드",
      description = "즉시 다운로드에 대한 여러 컨텐츠 파일을 한 번에 업로드합니다. 비어있지 않은 파일만 처리합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "여러 파일 업로드 성공",
        content = @Content(schema = @Schema(implementation = List.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 - 파일이 없거나 모든 파일이 비어있는 경우"),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "413", description = "파일 크기 제한 초과"),
    @ApiResponse(responseCode = "500", description = "서버 오류 - 파일 저장 실패")
  })
  @PostMapping(value = "/direct-contents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<GrobleResponse<List<FileUploadResponse>>> uploadContentsFiles(
      @Auth Accessor accessor,
      @RequestParam("files") List<MultipartFile> files,
      @RequestParam(value = "directory", defaultValue = "contents") String directory) {

    try {
      List<FileUploadResponse> responses = new ArrayList<>();

      for (MultipartFile file : files) {
        if (file != null && !file.isEmpty()) {
          FileUploadDto fileUploadDto = fileDtoMapper.toServiceFileUploadDto(file, directory);
          FileDto fileDto = fileService.uploadFile(accessor.getUserId(), fileUploadDto);

          responses.add(
              FileUploadResponse.of(
                  fileDto.getOriginalFilename(),
                  fileDto.getFileUrl(),
                  fileDto.getContentType(),
                  directory));
        }
      }

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(GrobleResponse.success(responses, "파일 업로드가 성공적으로 완료되었습니다.", 201));
    } catch (IOException e) {
      log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
      throw new FileProcessingException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  /** 파일 타입에 따른 디렉토리 결정 */
  private String getDirectoryByFileType(String fileType) {
    if (fileType == null) {
      return DEFAULT_DIRECTORY;
    }

    return switch (fileType.toUpperCase()) {
      case "IMAGE" -> IMAGE_DIRECTORY;
      case "DOCUMENT", "PDF", "WORD", "EXCEL", "PPT" -> DOCUMENT_DIRECTORY;
      case "CONTENT" -> CONTENT_DIRECTORY;
      default -> DEFAULT_DIRECTORY;
    };
  }

  /** 이미지 파일 여부 확인 */
  private boolean isImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image/");
  }
}
