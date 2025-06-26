package liaison.groble.api.server.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.terms.request.MakerTermsAgreementRequest;
import liaison.groble.api.model.terms.response.MakerTermsAgreementResponse;
import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;
import liaison.groble.application.terms.service.TermsService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.service.ClientInfoService;
import liaison.groble.mapping.terms.TermsMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/maker")
@Tag(name = "[메이커] 메이커 이용 약관 동의", description = "구매자에서 메이커로 전환하기 위한 약관 동의 API입니다.")
public class MakerController {
  private final TermsMapper termsMapper;
  private final TermsService termsService;
  private final ClientInfoService clientInfoService; // 클라이언트 정보 서비스 주입

  /** 메이커 이용약관 동의 API */
  @Operation(summary = "메이커 이용약관 동의", description = "메이커(판매자)로 활동하기 위한 이용약관에 동의합니다.")
  @PostMapping("/terms/agree")
  public ResponseEntity<GrobleResponse<MakerTermsAgreementResponse>> agreeMakerTerms(
      @Auth Accessor accessor,
      @Parameter(description = "메이커 약관 동의 정보", required = true) @Valid @RequestBody
          MakerTermsAgreementRequest request,
      @RequestHeader("User-Agent") String userAgent, // User-Agent 직접 받기
      HttpServletRequest httpRequest) { // IP 주소 추출을 위해 유지

    log.info(
        "메이커 이용약관 동의 요청: userId={}, agreed={}", accessor.getId(), request.getMakerTermsAgreement());

    // 클라이언트 IP 추출 로직을 서비스로 위임
    String clientIp = clientInfoService.getClientIpAddress(httpRequest);

    // DTO 변환 및 서비스 호출
    MakerTermsAgreementDTO agreementDto = termsMapper.toMakerTermsAgreementDTO(request);
    MakerTermsAgreementDTO result =
        termsService.agreeMakerTerms(accessor.getUserId(), agreementDto, clientIp, userAgent);

    // 응답 생성
    MakerTermsAgreementResponse response =
        MakerTermsAgreementResponse.of(accessor.getUserId(), result.getMakerTermsAgreement());

    return ResponseEntity.ok(GrobleResponse.success(response, "메이커 이용약관 동의가 완료되었습니다.", 200));
  }
}
