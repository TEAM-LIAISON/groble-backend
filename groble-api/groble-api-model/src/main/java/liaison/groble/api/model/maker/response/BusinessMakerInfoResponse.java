package liaison.groble.api.model.maker.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사업자 메이커 상세 정보")
public class BusinessMakerInfoResponse {

  @Schema(description = "상호명", example = "링킷")
  private String businessName;

  @Schema(description = "대표자명", example = "김철수")
  private String representativeName;

  @Schema(description = "사업자등록번호", example = "123-45-67890")
  private String businessNumber;

  @Schema(description = "사업자 소재지", example = "서울특별시 강남구 테헤란로 123 4F")
  private String businessAddress;
}
