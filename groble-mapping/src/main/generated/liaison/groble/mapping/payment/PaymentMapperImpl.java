package liaison.groble.mapping.payment;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaypleAuthResultRequest;
import liaison.groble.api.model.payment.response.PaymentCancelInfoResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelInfoDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-21T12:48:50+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class PaymentMapperImpl implements PaymentMapper {

  @Override
  public PaypleAuthResultDTO toPaypleAuthResultDTO(
      PaypleAuthResultRequest paypleAuthResultRequest) {
    if (paypleAuthResultRequest == null) {
      return null;
    }

    PaypleAuthResultDTO.PaypleAuthResultDTOBuilder paypleAuthResultDTO =
        PaypleAuthResultDTO.builder();

    if (paypleAuthResultRequest.getPayRst() != null) {
      paypleAuthResultDTO.payRst(paypleAuthResultRequest.getPayRst());
    }
    if (paypleAuthResultRequest.getPcdPayMethod() != null) {
      paypleAuthResultDTO.pcdPayMethod(paypleAuthResultRequest.getPcdPayMethod());
    }
    if (paypleAuthResultRequest.getPayCode() != null) {
      paypleAuthResultDTO.payCode(paypleAuthResultRequest.getPayCode());
    }
    if (paypleAuthResultRequest.getPayMsg() != null) {
      paypleAuthResultDTO.payMsg(paypleAuthResultRequest.getPayMsg());
    }
    if (paypleAuthResultRequest.getPayType() != null) {
      paypleAuthResultDTO.payType(paypleAuthResultRequest.getPayType());
    }
    if (paypleAuthResultRequest.getCardVer() != null) {
      paypleAuthResultDTO.cardVer(paypleAuthResultRequest.getCardVer());
    }
    if (paypleAuthResultRequest.getPayWork() != null) {
      paypleAuthResultDTO.payWork(paypleAuthResultRequest.getPayWork());
    }
    if (paypleAuthResultRequest.getAuthKey() != null) {
      paypleAuthResultDTO.authKey(paypleAuthResultRequest.getAuthKey());
    }
    if (paypleAuthResultRequest.getPayReqKey() != null) {
      paypleAuthResultDTO.payReqKey(paypleAuthResultRequest.getPayReqKey());
    }
    if (paypleAuthResultRequest.getPayReqTime() != null) {
      paypleAuthResultDTO.payReqTime(paypleAuthResultRequest.getPayReqTime());
    }
    if (paypleAuthResultRequest.getPayHost() != null) {
      paypleAuthResultDTO.payHost(paypleAuthResultRequest.getPayHost());
    }
    if (paypleAuthResultRequest.getPayCofUrl() != null) {
      paypleAuthResultDTO.payCofUrl(paypleAuthResultRequest.getPayCofUrl());
    }
    if (paypleAuthResultRequest.getPayDiscount() != null) {
      paypleAuthResultDTO.payDiscount(paypleAuthResultRequest.getPayDiscount());
    }
    if (paypleAuthResultRequest.getPayEasyPayMethod() != null) {
      paypleAuthResultDTO.payEasyPayMethod(paypleAuthResultRequest.getPayEasyPayMethod());
    }
    if (paypleAuthResultRequest.getEasyPayMethod() != null) {
      paypleAuthResultDTO.easyPayMethod(paypleAuthResultRequest.getEasyPayMethod());
    }
    if (paypleAuthResultRequest.getPayerNo() != null) {
      paypleAuthResultDTO.payerNo(paypleAuthResultRequest.getPayerNo());
    }
    if (paypleAuthResultRequest.getPayAmount() != null) {
      paypleAuthResultDTO.payAmount(paypleAuthResultRequest.getPayAmount());
    }
    if (paypleAuthResultRequest.getPayAmountReal() != null) {
      paypleAuthResultDTO.payAmountReal(paypleAuthResultRequest.getPayAmountReal());
    }
    if (paypleAuthResultRequest.getPayerName() != null) {
      paypleAuthResultDTO.payerName(paypleAuthResultRequest.getPayerName());
    }
    if (paypleAuthResultRequest.getPayerHp() != null) {
      paypleAuthResultDTO.payerHp(paypleAuthResultRequest.getPayerHp());
    }
    if (paypleAuthResultRequest.getPayerId() != null) {
      paypleAuthResultDTO.payerId(paypleAuthResultRequest.getPayerId());
    }
    if (paypleAuthResultRequest.getPayerEmail() != null) {
      paypleAuthResultDTO.payerEmail(paypleAuthResultRequest.getPayerEmail());
    }
    if (paypleAuthResultRequest.getPayOid() != null) {
      paypleAuthResultDTO.payOid(paypleAuthResultRequest.getPayOid());
    }
    if (paypleAuthResultRequest.getPayGoods() != null) {
      paypleAuthResultDTO.payGoods(paypleAuthResultRequest.getPayGoods());
    }
    if (paypleAuthResultRequest.getPayTotal() != null) {
      paypleAuthResultDTO.payTotal(paypleAuthResultRequest.getPayTotal());
    }
    if (paypleAuthResultRequest.getPayTaxTotal() != null) {
      paypleAuthResultDTO.payTaxTotal(paypleAuthResultRequest.getPayTaxTotal());
    }
    if (paypleAuthResultRequest.getPayIsTax() != null) {
      paypleAuthResultDTO.payIsTax(paypleAuthResultRequest.getPayIsTax());
    }
    if (paypleAuthResultRequest.getPayCardName() != null) {
      paypleAuthResultDTO.payCardName(paypleAuthResultRequest.getPayCardName());
    }
    if (paypleAuthResultRequest.getPayCardNum() != null) {
      paypleAuthResultDTO.payCardNum(paypleAuthResultRequest.getPayCardNum());
    }
    if (paypleAuthResultRequest.getPayCardQuota() != null) {
      paypleAuthResultDTO.payCardQuota(paypleAuthResultRequest.getPayCardQuota());
    }
    if (paypleAuthResultRequest.getPayCardTradeNum() != null) {
      paypleAuthResultDTO.payCardTradeNum(paypleAuthResultRequest.getPayCardTradeNum());
    }
    if (paypleAuthResultRequest.getPayCardAuthNo() != null) {
      paypleAuthResultDTO.payCardAuthNo(paypleAuthResultRequest.getPayCardAuthNo());
    }
    if (paypleAuthResultRequest.getPayCardReceipt() != null) {
      paypleAuthResultDTO.payCardReceipt(paypleAuthResultRequest.getPayCardReceipt());
    }
    if (paypleAuthResultRequest.getPayTime() != null) {
      paypleAuthResultDTO.payTime(paypleAuthResultRequest.getPayTime());
    }
    if (paypleAuthResultRequest.getRegulerFlag() != null) {
      paypleAuthResultDTO.regulerFlag(paypleAuthResultRequest.getRegulerFlag());
    }
    if (paypleAuthResultRequest.getPayYear() != null) {
      paypleAuthResultDTO.payYear(paypleAuthResultRequest.getPayYear());
    }
    if (paypleAuthResultRequest.getPayMonth() != null) {
      paypleAuthResultDTO.payMonth(paypleAuthResultRequest.getPayMonth());
    }
    if (paypleAuthResultRequest.getSimpleFlag() != null) {
      paypleAuthResultDTO.simpleFlag(paypleAuthResultRequest.getSimpleFlag());
    }
    if (paypleAuthResultRequest.getRstUrl() != null) {
      paypleAuthResultDTO.rstUrl(paypleAuthResultRequest.getRstUrl());
    }
    if (paypleAuthResultRequest.getUserDefine1() != null) {
      paypleAuthResultDTO.userDefine1(paypleAuthResultRequest.getUserDefine1());
    }
    if (paypleAuthResultRequest.getUserDefine2() != null) {
      paypleAuthResultDTO.userDefine2(paypleAuthResultRequest.getUserDefine2());
    }
    if (paypleAuthResultRequest.getPcdPayUrl() != null) {
      paypleAuthResultDTO.pcdPayUrl(paypleAuthResultRequest.getPcdPayUrl());
    }

    return paypleAuthResultDTO.build();
  }

  @Override
  public PaymentCancelDTO toPaymentCancelDTO(PaymentCancelRequest paymentCancelRequest) {
    if (paymentCancelRequest == null) {
      return null;
    }

    PaymentCancelDTO.PaymentCancelDTOBuilder paymentCancelDTO = PaymentCancelDTO.builder();

    if (paymentCancelRequest.getCancelReason() != null) {
      paymentCancelDTO.cancelReason(paymentCancelRequest.getCancelReason().name());
    }
    if (paymentCancelRequest.getDetailReason() != null) {
      paymentCancelDTO.detailReason(paymentCancelRequest.getDetailReason());
    }

    return paymentCancelDTO.build();
  }

  @Override
  public PaymentCancelInfoResponse toPaymentCancelInfoResponse(
      PaymentCancelInfoDTO paymentCancelInfoDTO) {
    if (paymentCancelInfoDTO == null) {
      return null;
    }

    PaymentCancelInfoResponse.PaymentCancelInfoResponseBuilder paymentCancelInfoResponse =
        PaymentCancelInfoResponse.builder();

    if (paymentCancelInfoDTO.getMerchantUid() != null) {
      paymentCancelInfoResponse.merchantUid(paymentCancelInfoDTO.getMerchantUid());
    }
    if (paymentCancelInfoDTO.getOriginalPrice() != null) {
      paymentCancelInfoResponse.originalPrice(paymentCancelInfoDTO.getOriginalPrice());
    }
    if (paymentCancelInfoDTO.getDiscountPrice() != null) {
      paymentCancelInfoResponse.discountPrice(paymentCancelInfoDTO.getDiscountPrice());
    }
    if (paymentCancelInfoDTO.getFinalPrice() != null) {
      paymentCancelInfoResponse.finalPrice(paymentCancelInfoDTO.getFinalPrice());
    }
    if (paymentCancelInfoDTO.getPayType() != null) {
      paymentCancelInfoResponse.payType(paymentCancelInfoDTO.getPayType());
    }
    if (paymentCancelInfoDTO.getPayCardName() != null) {
      paymentCancelInfoResponse.payCardName(paymentCancelInfoDTO.getPayCardName());
    }
    if (paymentCancelInfoDTO.getPayCardNum() != null) {
      paymentCancelInfoResponse.payCardNum(paymentCancelInfoDTO.getPayCardNum());
    }

    return paymentCancelInfoResponse.build();
  }
}
