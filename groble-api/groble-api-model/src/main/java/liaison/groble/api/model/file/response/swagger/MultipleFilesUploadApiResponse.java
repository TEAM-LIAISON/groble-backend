package liaison.groble.api.model.file.response.swagger;

import java.util.List;

import liaison.groble.api.model.file.response.FileUploadResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MultipleFilesUploadApiResponse")
public class MultipleFilesUploadApiResponse extends GrobleResponse<List<FileUploadResponse>> {}
