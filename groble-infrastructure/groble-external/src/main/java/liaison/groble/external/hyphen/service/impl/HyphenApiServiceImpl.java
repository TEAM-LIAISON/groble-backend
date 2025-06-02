package liaison.groble.external.hyphen.service.impl;

import org.springframework.stereotype.Service;

import liaison.groble.external.hyphen.client.HyphenApiClient;
import liaison.groble.external.hyphen.dto.request.*;
import liaison.groble.external.hyphen.dto.response.*;
import liaison.groble.external.hyphen.service.HyphenApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class HyphenApiServiceImpl implements HyphenApiService {

  private final HyphenApiClient hyphenApiClient;

  @Override
  public Mono<HyphenAccountVerificationResponse> verifyAccountOwner(
      String bankCode, String accountNumber, String idNumber) {

    log.info("계좌 실명 인증 요청 - 은행코드: {}, 계좌번호: {}", bankCode, accountNumber);

    HyphenAccountVerificationRequest request =
        HyphenAccountVerificationRequest.builder()
            .bankCd(bankCode)
            .acctNo(accountNumber)
            .idNo(idNumber)
            .build();

    return hyphenApiClient
        .verifyAccount(request)
        .doOnSuccess(
            response -> {
              if (response.isSuccess()) {
                log.info("계좌 실명 인증 성공 - 예금주: {}", response.getName());
              } else {
                log.warn(
                    "계좌 실명 인증 실패 - 응답코드: {}, 메시지: {}", response.getReply(), response.getReplyMsg());
              }
            })
        .doOnError(error -> log.error("계좌 실명 인증 오류", error));
  }

  @Override
  public Mono<HyphenOneWonTransferResponse> requestOneWonTransfer(
      String bankCode, String accountNumber, String msgType, String compName, String printContent) {

    log.info("1원 송금 요청 - 은행코드: {}, 계좌번호: {}, 인증타입: {}", bankCode, accountNumber, msgType);

    HyphenOneWonTransferRequest.HyphenOneWonTransferRequestBuilder builder =
        HyphenOneWonTransferRequest.builder()
            .inBankCode(bankCode)
            .inAccount(accountNumber)
            .msgType(msgType != null ? msgType : "1");

    // msgType에 따른 필수값 설정
    if ("2".equals(msgType) && compName != null) {
      builder.compName(compName);
    } else if ("3".equals(msgType) && printContent != null) {
      builder.printContent(printContent);
    }

    return hyphenApiClient
        .requestOneWonTransfer(builder.build())
        .doOnSuccess(
            response -> {
              if (response.isSuccess()) {
                log.info(
                    "1원 송금 성공 - 거래번호: {}, 적요: {}",
                    response.getOriSeqNo(),
                    response.getInPrintContent());
              } else {
                log.warn(
                    "1원 송금 실패 - 응답코드: {}, 메시지: {}",
                    response.getReplyCode(),
                    response.getReplyMessage());
              }
            })
        .doOnError(error -> log.error("1원 송금 요청 오류", error));
  }

  @Override
  public Mono<HyphenOneWonVerifyResponse> verifyOneWonTransfer(
      String oriSeqNo, String inputContent, String trDate) {

    log.info("1원 송금 검증 - 거래번호: {}, 입력적요: {}", oriSeqNo, inputContent);

    HyphenOneWonVerifyRequest request =
        HyphenOneWonVerifyRequest.builder()
            .oriSeqNo(oriSeqNo)
            .inPrintContent(inputContent)
            .trDate(trDate)
            .build();

    return hyphenApiClient
        .verifyOneWonTransfer(request)
        .doOnSuccess(
            response -> {
              if (response.isSuccess()) {
                log.info("1원 송금 검증 성공");
              } else {
                log.warn("1원 송금 검증 실패 - 에러: {}", response.getError());
              }
            })
        .doOnError(error -> log.error("1원 송금 검증 오류", error));
  }
}
