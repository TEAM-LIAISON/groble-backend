package liaison.groble.api.model.file.response.swagger;

import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FileUploadApiResponse")
public class FileUploadApiResponse extends GrobleResponse<FileUploadResponse> {}
