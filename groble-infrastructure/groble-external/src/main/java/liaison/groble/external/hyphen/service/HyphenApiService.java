package liaison.groble.external.hyphen.service;

import liaison.groble.external.hyphen.dto.request.*;
import liaison.groble.external.hyphen.dto.response.*;

import reactor.core.publisher.Mono;

public interface HyphenApiService {

  /**
   * 계좌 실명 인증
   *
   * @param bankCode 은행코드 (3자리)
   * @param accountNumber 계좌번호
   * @param idNumber 신원확인번호 (생년월일 6자리 or 사업자번호)
   * @return 계좌인증 결과
   */
  Mono<HyphenAccountVerificationResponse> verifyAccountOwner(
      String bankCode, String accountNumber, String idNumber);

  /**
   * 1원 송금 요청
   *
   * @param bankCode 은행코드
   * @param accountNumber 계좌번호
   * @param msgType 인증적요 유형 (1: 한글4자리, 2: 업체명+숫자3자리, 3: 직접입력)
   * @param compName 업체명 (msgType 2일때 필수)
   * @param printContent 인증적요 직접입력 (msgType 3일때 필수)
   * @return 1원송금 요청 결과
   */
  Mono<HyphenOneWonTransferResponse> requestOneWonTransfer(
      String bankCode, String accountNumber, String msgType, String compName, String printContent);

  /**
   * 1원 송금 검증
   *
   * @param oriSeqNo 원거래 일련번호 (1원송금 요청시 받은 값)
   * @param inputContent 사용자 입력 적요
   * @param trDate 거래날짜 (yyyy-MM-dd)
   * @return 검증 결과
   */
  Mono<HyphenOneWonVerifyResponse> verifyOneWonTransfer(
      String oriSeqNo, String inputContent, String trDate);
}
