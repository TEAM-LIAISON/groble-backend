package liaison.groble.api.model.scrap.response.swagger;

import liaison.groble.api.model.scrap.response.UpdateContentScrapStateResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateContentScrapStateApiResponse")
public class UpdateContentScrapStateApiResponse
    extends GrobleResponse<UpdateContentScrapStateResponse> {}
