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

  // 파일 타입별 디렉토리 매핑 (확장성 고려)
  private static final String DEFAULT_DIRECTORY = "uploads";
  private static final String IMAGE_DIRECTORY = "images";
  private static final String DOCUMENT_DIRECTORY = "documents";
  private static final String CONTENT_DIRECTORY = "contents";

  /** 기본 파일 업로드 - 모든 파일 타입 처리 */
  @Operation(summary = "파일 업로드", description = "폼 데이터를 통해 파일을 업로드합니다.")
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

  /** 컨텐츠 대표 이미지 업로드 (기존 코드 유지) */
  @Operation(summary = "컨텐츠 대표 이미지 업로드", description = "컨텐츠 대표 이미지를 업로드합니다.")
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

  /** 여러 파일 업로드 (기존 코드 개선) */
  @Operation(summary = "컨텐츠 파일 업로드", description = "즉시 다운로드에 대한 여러 컨텐츠 파일을 업로드합니다.")
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

    switch (fileType.toUpperCase()) {
      case "IMAGE":
        return IMAGE_DIRECTORY;
      case "DOCUMENT":
      case "PDF":
      case "WORD":
      case "EXCEL":
      case "PPT":
        return DOCUMENT_DIRECTORY;
      case "CONTENT":
        return CONTENT_DIRECTORY;
      default:
        return DEFAULT_DIRECTORY;
    }
  }

  /** 이미지 파일 여부 확인 */
  private boolean isImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image/");
  }
}
