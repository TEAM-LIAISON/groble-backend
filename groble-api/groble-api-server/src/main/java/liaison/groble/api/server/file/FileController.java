package liaison.groble.api.server.file;

import java.io.IOException;
import java.util.Map;

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

  @Operation(summary = "파일 업로드", description = "폼 데이터를 통해 파일을 업로드합니다.")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<GrobleResponse<FileUploadResponse>> uploadFile(
      @Auth Accessor accessor,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "directory", defaultValue = "uploads") String directory,
      @RequestParam(required = false) Map<String, String> params)
      throws IOException {

    FileUploadDto fileUploadDto = fileDtoMapper.toServiceFileUploadDto(file, directory);
    FileDto fileDto = fileService.uploadFile(accessor.getUserId(), fileUploadDto);

    FileUploadResponse fileUploadResponse =
        FileUploadResponse.of(
            fileDto.getOriginalFilename(),
            fileDto.getFileUrl(),
            fileDto.getContentType(),
            directory);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(GrobleResponse.success(fileUploadResponse, "파일 업로드가 성공적으로 완료되었습니다.", 201));
  }
}
