package liaison.groble.application.payment.dto.link;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaypleLinkResponse {
  private String linkRst; // 링크 생성 결과 (success/error)
  private String linkMsg; // 링크 생성 메시지
  private String linkKey; // 링크 고유 키
  private String linkUrl; // 결제 링크 URL
  private String linkOid; // 주문번호
  private String linkGoods; // 상품명
  private String linkTotal; // 결제금액
  private String linkTime; // 링크 생성 시간
  private String linkExpire; // 링크 만료 시간
}
