package liaison.groble.external.hyphen.controller;

import org.springframework.web.bind.annotation.*;

import liaison.groble.external.hyphen.dto.response.HyphenAccountVerificationResponse;
import liaison.groble.external.hyphen.dto.response.HyphenOneWonTransferResponse;
import liaison.groble.external.hyphen.dto.response.HyphenOneWonVerifyResponse;
import liaison.groble.external.hyphen.service.HyphenApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/** Hyphen API 테스트용 컨트롤러 실제 운영 환경에서는 사용하지 않음 */
@Slf4j
@RestController
@RequestMapping("/test/hyphen")
@RequiredArgsConstructor
public class HyphenTestController {

  private final HyphenApiService hyphenApiService;

  @PostMapping("/account/verify")
  public Mono<HyphenAccountVerificationResponse> verifyAccount(
      @RequestParam String bankCode,
      @RequestParam String accountNumber,
      @RequestParam String idNumber) {

    return hyphenApiService.verifyAccountOwner(bankCode, accountNumber, idNumber);
  }

  @PostMapping("/transfer/request")
  public Mono<HyphenOneWonTransferResponse> requestTransfer(
      @RequestParam String bankCode,
      @RequestParam String accountNumber,
      @RequestParam(defaultValue = "1") String msgType,
      @RequestParam(required = false) String compName,
      @RequestParam(required = false) String printContent) {

    return hyphenApiService.requestOneWonTransfer(
        bankCode, accountNumber, msgType, compName, printContent);
  }

  @PostMapping("/transfer/verify")
  public Mono<HyphenOneWonVerifyResponse> verifyTransfer(
      @RequestParam String oriSeqNo,
      @RequestParam String inputContent,
      @RequestParam String trDate) {

    return hyphenApiService.verifyOneWonTransfer(oriSeqNo, inputContent, trDate);
  }
}
