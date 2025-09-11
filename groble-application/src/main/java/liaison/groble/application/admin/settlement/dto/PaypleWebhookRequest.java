package liaison.groble.application.admin.settlement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 페이플 이체 실행 결과 웹훅 수신용 DTO
 *
 * <p>페이플에서 전송하는 이체 실행 결과를 수신하기 위한 데이터 전송 객체입니다. 실제 웹훅 응답 스펙에 맞게 구성되었습니다.
 */
@Getter
@Setter
@ToString(exclude = {"billingTranId", "groupKey", "apiTranId", "accountNum"}) // 민감한 정보는 로그에서 제외
public class PaypleWebhookRequest {

  /** 페이플 응답코드 (A0000: 성공) */
  private String result;

  /** 응답 메시지 */
  private String message;

  /** 파트너 ID */
  @JsonProperty("cst_id")
  private String cstId;

  /** 파트너 하위 셀러 ID */
  @JsonProperty("sub_id")
  private String subId;

  /** 중복 이체 방지 키 */
  @JsonProperty("distinct_key")
  private String distinctKey;

  /** 그룹키 */
  @JsonProperty("group_key")
  private String groupKey;

  /** 인증 완료된 계좌의 빌링키 */
  @JsonProperty("billing_tran_id")
  private String billingTranId;

  /** 완료된 이체 건의 고유키 */
  @JsonProperty("api_tran_id")
  private String apiTranId;

  /** 금융기관으로부터 수신한 이체 완료 상세일시 (밀리세컨드) */
  @JsonProperty("api_tran_dtm")
  private String apiTranDtm;

  /** 금융기관과 통신을 위해 필요한 고유 키 */
  @JsonProperty("bank_tran_id")
  private String bankTranId;

  /** 금융기관으로부터 수신한 인증 완료 일자 */
  @JsonProperty("bank_tran_date")
  private String bankTranDate;

  /** 금융기관 응답코드 */
  @JsonProperty("bank_rsp_code")
  private String bankRspCode;

  /** 금융기관 코드 */
  @JsonProperty("bank_code_std")
  private String bankCodeStd;

  /** 금융기관 점별 코드 (3자리 + 4자리) */
  @JsonProperty("bank_code_sub")
  private String bankCodeSub;

  /** 금융기관명 */
  @JsonProperty("bank_name")
  private String bankName;

  /** 계좌번호 */
  @JsonProperty("account_num")
  private String accountNum;

  /** 일부 가림 처리된 계좌번호 */
  @JsonProperty("account_num_masked")
  private String accountNumMasked;

  /** 예금주명 */
  @JsonProperty("account_holder_name")
  private String accountHolderName;

  /** 상대방 계좌 거래 내역에 표시될 문구 (최대 6자) */
  @JsonProperty("print_content")
  private String printContent;

  /** 이체 완료 금액 */
  @JsonProperty("tran_amt")
  private String tranAmt;

  /**
   * 이체 실행이 성공했는지 확인
   *
   * @return 성공 여부
   */
  public boolean isSuccess() {
    return "A0000".equals(result);
  }

  /**
   * 이체 실행이 실패했는지 확인
   *
   * @return 실패 여부
   */
  public boolean isFailed() {
    return !"A0000".equals(result);
  }
}
