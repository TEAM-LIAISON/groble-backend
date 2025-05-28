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

import liaison.groble.api.model.content.response.swagger.UploadContentThumbnail;
import liaison.groble.api.model.file.response.FileUploadResponse;
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

@Deprecated
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "이미지 및 첨부 파일 API", description = "파일 업로드 및 관리 API")
public class FileController {
  private final FileService fileService;
  private final FileDtoMapper fileDtoMapper;

  @Deprecated
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

  // 썸네일 이미지를 업로드해서 URL 경로를 반환 받음
  @Deprecated
  @UploadContentThumbnail
  @PostMapping(value = "/content/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<GrobleResponse<FileUploadResponse>> uploadContentThumbnail(
      @Auth Accessor accessor,
      @RequestParam("file") MultipartFile file,
      @RequestParam(
              value = "directory",
              defaultValue = "images/contents/thumbnail",
              required = false)
          String directory) {

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

  @Deprecated
  @UploadMultipleFiles
  @PostMapping(value = "/content/direct-contents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

  /** 이미지 파일 여부 확인 */
  private boolean isImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image/");
  }
}
