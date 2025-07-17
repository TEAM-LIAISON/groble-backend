package liaison.groble.api.model.notification.response.swagger;

import liaison.groble.api.model.notification.response.NotificationItemsResponse;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NotificationItemsApiResponse")
public class NotificationItemsApiResponse extends GrobleResponse<NotificationItemsResponse> {}
