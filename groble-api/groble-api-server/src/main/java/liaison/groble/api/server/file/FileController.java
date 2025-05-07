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
import liaison.groble.api.model.file.response.swagger.UploadContentThumbnail;
import liaison.groble.api.model.file.response.swagger.UploadFile;
import liaison.groble.api.model.file.response.swagger.UploadMultipleFiles;
import liaison.groble.api.server.file.mapper.FileDtoMapper;
import liaison.groble.application.file.FileService;
import liaison.groble.application.file.dto.FileDto;
import liaison.groble.application.file.dto.FileUploadDto;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.exception.FileProcessingException;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "이미지 및 첨부 파일 API", description = "파일 업로드 및 관리 API")
public class FileController {
  private final FileService fileService;
  private final FileDtoMapper fileDtoMapper;

  private static final String DEFAULT_DIRECTORY = "default";
  private static final String IMAGE_DIRECTORY = "images";
  private static final String CONTENT_DIRECTORY = "contents";
  private static final String BUSINESS_LICENSE_DIRECTORY = "business-license";

  @UploadFile
  @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<GrobleResponse<FileUploadResponse>> uploadFile(
      @Auth Accessor accessor,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "fileType", required = false) String fileType,
      @RequestParam(value = "directory", required = false) String directory) {

    try {
      String targetDirectory = fileService.resolveDirectoryPath(fileType, directory);

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

  @UploadContentThumbnail
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
          .body(GrobleResponse.success(response, "이미지 업로드가 성공적으로 완료되었습니다.", 201));
    } catch (IOException e) {
      log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
      throw new FileProcessingException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  @UploadMultipleFiles
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
      case "DOCUMENT", "PDF", "WORD", "EXCEL", "PPT" -> CONTENT_DIRECTORY;
      default -> DEFAULT_DIRECTORY;
    };
  }

  /** 파일 타입과 디렉토리를 조합하여 최종 저장 경로 결정 */
  private String determineTargetDirectory(String fileType, String directory) {
    String typeDirectory = getDirectoryByFileType(fileType);

    // 디렉토리가 지정되지 않은 경우 타입 디렉토리만 사용
    if (directory == null) {
      return typeDirectory;
    }

    // fileType이 IMAGE인 경우 images/지정된디렉토리 형식으로 조합
    if (fileType != null && fileType.equalsIgnoreCase("IMAGE")) {
      return typeDirectory + "/" + directory.toLowerCase();
    }

    // 그 외의 경우 지정된 디렉토리만 사용
    return directory;
  }

  /** 이미지 파일 여부 확인 */
  private boolean isImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image/");
  }
}
