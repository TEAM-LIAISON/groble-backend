package liaison.groble.api.model.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[관리자 주문 페이지] 주문 취소 요청 처리 응답 DTO")
public class AdminOrderCancelRequestResponse {}
