package liaison.groble.api.server.terms;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.terms.request.MakerTermsAgreementRequest;
import liaison.groble.api.model.terms.request.TermsAgreementRequest;
import liaison.groble.api.model.terms.response.MakerTermsAgreementResponse;
import liaison.groble.api.model.terms.response.TermsAgreementResponse;
import liaison.groble.api.model.user.request.AdvertisingAgreementRequest;
import liaison.groble.api.server.terms.mapper.TermsDtoMapper;
import liaison.groble.application.terms.dto.MakerTermsAgreementDto;
import liaison.groble.application.terms.dto.TermsAgreementDto;
import liaison.groble.application.terms.service.TermsService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 약관 관련 API 컨트롤러 */
@Slf4j
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
@Tag(name = "약관 정보 API", description = "약관 동의, 철회 및 조회 API")
public class TermsController {
  private final TermsService termsService;
  private final TermsDtoMapper termsDtoMapper;

  @Operation(summary = "회원가입 약관 동의", description = "사용자가 회원가입 과정에서 약관에 동의합니다.")
  @PostMapping("/agree")
  public ResponseEntity<GrobleResponse<TermsAgreementResponse>> agreeToTerms(
      @Auth Accessor accessor,
      @Valid @RequestBody TermsAgreementRequest request,
      HttpServletRequest httpRequest) {

    log.info("약관 동의 요청: {} -> 약관: {}", accessor.getEmail(), request.getTermsTypes());

    // 1. API DTO → 서비스 DTO 변환
    TermsAgreementDto termsAgreementDto = termsDtoMapper.toServiceTermsAgreementDto(request);
    termsAgreementDto.setUserId(accessor.getUserId());

    // IP 및 User-Agent 설정
    termsAgreementDto.setIpAddress(httpRequest.getRemoteAddr());
    termsAgreementDto.setUserAgent(httpRequest.getHeader("User-Agent"));

    // 2. 서비스 호출
    TermsAgreementDto resultDto = termsService.agreeToTerms(termsAgreementDto);

    // 3. 서비스 DTO → API DTO 변환
    TermsAgreementResponse response = termsDtoMapper.toApiTermsAgreementResponse(resultDto);

    // 4. API 응답 생성
    return ResponseEntity.ok(GrobleResponse.success(response, "약관 동의가 처리되었습니다."));
  }

  @Operation(summary = "약관 동의 철회", description = "사용자가 동의한 약관을 철회합니다. 필수 약관은 철회할 수 없습니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "약관 동의 철회 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    @ApiResponse(responseCode = "403", description = "필수 약관은 철회할 수 없습니다.")
  })
  @PostMapping("/withdraw")
  @Logging(item = "Terms", action = "WITHDRAW", includeParam = true)
  public ResponseEntity<GrobleResponse<TermsAgreementResponse>> withdrawTermsAgreement(
      @Auth Accessor accessor,
      @Valid @RequestBody TermsAgreementRequest request,
      HttpServletRequest httpRequest) {

    log.info("약관 철회 요청: {} -> 약관: {}", accessor.getEmail(), request.getTermsTypes());

    // 1. API DTO → 서비스 DTO 변환
    TermsAgreementDto termsAgreementDto = termsDtoMapper.toServiceTermsAgreementDto(request);
    termsAgreementDto.setUserId(accessor.getUserId());

    // IP 및 User-Agent 설정
    termsAgreementDto.setIpAddress(httpRequest.getRemoteAddr());
    termsAgreementDto.setUserAgent(httpRequest.getHeader("User-Agent"));

    // 2. 서비스 호출
    TermsAgreementDto resultDto = termsService.withdrawTermsAgreement(termsAgreementDto);

    // 3. 서비스 DTO → API DTO 변환
    TermsAgreementResponse response = termsDtoMapper.toApiTermsAgreementResponse(resultDto);

    // 4. API 응답 생성
    return ResponseEntity.ok(GrobleResponse.success(response, "약관 동의 철회가 처리되었습니다."));
  }

  /** 메이커 이용약관 동의 API */
  @Operation(summary = "메이커 이용약관 동의", description = "메이커(판매자)로 활동하기 위한 이용약관에 동의합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "약관 동의 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @PostMapping("/maker/terms/agree")
  public ResponseEntity<GrobleResponse<MakerTermsAgreementResponse>> agreeMakerTerms(
      @Auth Accessor accessor,
      @Parameter(description = "메이커 약관 동의 정보", required = true) @Valid @RequestBody
          MakerTermsAgreementRequest request,
      HttpServletRequest httpRequest) {

    log.info(
        "메이커 이용약관 동의 요청: userId={}, agreed={}", accessor.getId(), request.getMakerTermsAgreement());

    // 동의하지 않은 경우 에러
    if (!request.getMakerTermsAgreement()) {
      throw new IllegalArgumentException("메이커 이용약관에 동의해야 합니다.");
    }

    // 클라이언트 정보 추출
    String clientIp = getClientIpAddress(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    // 서비스 호출
    MakerTermsAgreementDto agreementDto =
        MakerTermsAgreementDto.builder()
            .userId(accessor.getId())
            .makerTermsAgreement(request.getMakerTermsAgreement())
            .build();

    MakerTermsAgreementDto result = termsService.agreeMakerTerms(agreementDto, clientIp, userAgent);

    // 응답 생성
    MakerTermsAgreementResponse response =
        MakerTermsAgreementResponse.of(result.getUserId(), result.getMakerTermsAgreement());

    return ResponseEntity.ok(GrobleResponse.success(response, "메이커 이용약관 동의가 완료되었습니다.", 200));
  }

  @Operation(summary = "사용자 약관 동의 상태 조회", description = "사용자의 약관 동의 상태를 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "약관 동의 상태 조회 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class)))
  })
  @GetMapping("/user")
  public ResponseEntity<GrobleResponse<List<TermsAgreementResponse>>> getUserTermsAgreements(
      @Auth Accessor accessor) {

    // 서비스 호출
    List<TermsAgreementDto> agreementDtos =
        termsService.getUserTermsAgreements(accessor.getUserId());

    // 서비스 DTO → API DTO 변환
    List<TermsAgreementResponse> responses =
        termsDtoMapper.toApiTermsAgreementResponseList(agreementDtos);

    return ResponseEntity.ok(GrobleResponse.success(responses));
  }

  @Operation(summary = "현재 유효한 약관 목록 조회", description = "현재 유효한 최신 버전의 약관 목록을 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "약관 목록 조회 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class)))
  })
  @GetMapping("/active")
  public ResponseEntity<GrobleResponse<List<TermsAgreementResponse>>> getActiveTerms() {

    // 서비스 호출
    List<TermsAgreementDto> termsDtos = termsService.getActiveTerms();

    // 서비스 DTO → API DTO 변환
    List<TermsAgreementResponse> responses =
        termsDtoMapper.toApiTermsAgreementResponseList(termsDtos);

    return ResponseEntity.ok(GrobleResponse.success(responses));
  }

  @Operation(summary = "광고성 정보 수신 동의 여부 조회", description = "현재 로그인한 사용자의 광고성 정보 수신 동의 여부를 조회합니다.")
  @GetMapping("/users/me/advertising-agreement")
  public ResponseEntity<GrobleResponse<Boolean>> getAdvertisingAgreementStatus(
      @Auth Accessor accessor) {
    // 1. 서비스 호출
    boolean agreed = termsService.getAdvertisingAgreementStatus(accessor.getUserId());
    return ResponseEntity.ok(GrobleResponse.success(agreed, "광고성 정보 수신 동의 여부 조회 성공"));
  }

  @Operation(summary = "광고성 정보 수신 동의 변경", description = "현재 로그인한 사용자의 광고성 정보 수신 동의 여부를 변경합니다.")
  @PostMapping("/users/me/advertising-agreement")
  public ResponseEntity<GrobleResponse<Void>> updateAdvertisingAgreementStatus(
      @Auth Accessor accessor,
      @Valid @RequestBody AdvertisingAgreementRequest request,
      HttpServletRequest httpRequest) {

    // 1. 서비스 호출
    termsService.updateAdvertisingAgreementStatus(
        accessor.getUserId(),
        request.getAgreed(),
        httpRequest.getRemoteAddr(),
        httpRequest.getHeader("User-Agent"));
    return ResponseEntity.ok(GrobleResponse.success(null, "광고성 정보 수신 동의 상태 변경 성공"));
  }

  /** 클라이언트 IP 주소 추출 */
  private String getClientIpAddress(HttpServletRequest request) {
    String[] headerNames = {
      "X-Forwarded-For",
      "X-Real-IP",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_CLIENT_IP",
      "HTTP_X_FORWARDED_FOR"
    };

    for (String headerName : headerNames) {
      String ip = request.getHeader(headerName);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        // X-Forwarded-For는 여러 IP가 콤마로 구분될 수 있음
        if (ip.contains(",")) {
          ip = ip.split(",")[0].trim();
        }
        return ip;
      }
    }

    return request.getRemoteAddr();
  }
}
