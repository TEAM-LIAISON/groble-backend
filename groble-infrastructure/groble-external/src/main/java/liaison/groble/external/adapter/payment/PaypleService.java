package liaison.groble.external.adapter.payment;

import java.util.Map;

import org.json.simple.JSONObject;

public interface PaypleService {

  JSONObject payAppCard(Map<String, String> params);

  JSONObject payConfirm(Map<String, String> params);

  JSONObject payAuth(Map<String, String> params);

  JSONObject payRefund(PaypleRefundRequest request);

  JSONObject paySimplePayment(Map<String, String> params);
}
