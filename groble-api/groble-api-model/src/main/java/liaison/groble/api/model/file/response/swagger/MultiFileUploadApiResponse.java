package liaison.groble.api.model.file.response.swagger;

import java.util.List;

import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MultiFileUploadApiResponse", description = "여러 개 파일 업로드 응답")
public class MultiFileUploadApiResponse extends GrobleResponse<List<FileUploadResponse>> {}
