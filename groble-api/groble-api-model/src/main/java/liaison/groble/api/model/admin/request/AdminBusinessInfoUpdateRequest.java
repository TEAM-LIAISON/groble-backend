package liaison.groble.api.model.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import liaison.groble.domain.user.enums.BusinessType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 사업자 정보 수정 요청")
public class AdminBusinessInfoUpdateRequest {

  @NotNull
  @Schema(
      description =
          "사업자 유형 (INDIVIDUAL_SIMPLIFIED: 개인사업자 간이과세자, INDIVIDUAL_NORMAL: 개인사업자 일반과세자, CORPORATE: 법인사업자)",
      example = "INDIVIDUAL_NORMAL")
  private BusinessType businessType;

  @NotBlank
  @Schema(description = "상호명", example = "그로블주식회사")
  private String businessName;

  @NotBlank
  @Schema(description = "대표자 이름", example = "홍길동")
  private String representativeName;

  @NotBlank
  @Schema(description = "사업장 소재지", example = "서울특별시 강남구 테헤란로 123")
  private String businessAddress;
}
