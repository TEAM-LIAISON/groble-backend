package liaison.groble.external.adapter.payment;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaypleServiceImpl implements PaypleService {

  private final PaypleConfig paypleConfig;

  @Override
  public JSONObject payAppCard(Map<String, String> params) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      // 앱카드 승인 요청 URL
      String appCardPaymentUrl = paypleConfig.getAppCardPaymentUrl();

      // 요청 파라미터 구성
      JSONObject obj = new JSONObject();
      obj.put("PCD_CST_ID", params.get("PCD_CST_ID"));
      obj.put("PCD_CUST_KEY", params.get("PCD_CUST_KEY"));
      obj.put("PCD_AUTH_KEY", params.get("PCD_AUTH_KEY"));
      obj.put("PCD_PAY_REQKEY", params.get("PCD_PAY_REQKEY"));

      log.info("페이플 앱카드 승인 요청: {}", obj.toString());

      URL url = new URL(appCardPaymentUrl);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("content-type", "application/json");
      con.setRequestProperty("charset", "UTF-8");
      con.setRequestProperty("referer", paypleConfig.getRefererUrl());
      con.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.write(obj.toString().getBytes());
      wr.flush();
      wr.close();

      int responseCode = con.getResponseCode();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      jsonObject = (JSONObject) jsonParser.parse(response.toString());

      log.info("페이플 앱카드 승인 응답: {}", jsonObject.toString());

    } catch (Exception e) {
      log.error("Payple 앱카드 결제 오류: ", e);
      jsonObject.put("PCD_PAY_RST", "error");
      jsonObject.put("PCD_PAY_MSG", "앱카드 결제 처리 중 오류가 발생했습니다: " + e.getMessage());
    }

    return jsonObject;
  }

  @Override
  public JSONObject payConfirm(Map<String, String> params) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      // 결제 승인 요청 URL
      String confirmUrl = paypleConfig.getPayConfirmUrl();

      // 요청 파라미터 구성
      JSONObject obj = new JSONObject();
      obj.put("PCD_CST_ID", params.get("PCD_CST_ID"));
      obj.put("PCD_CUST_KEY", params.get("PCD_CUST_KEY"));
      obj.put("PCD_AUTH_KEY", params.get("PCD_AUTH_KEY"));
      obj.put("PCD_PAY_REQKEY", params.get("PCD_PAY_REQKEY"));
      obj.put("PCD_PAYER_ID", params.get("PCD_PAYER_ID"));
      obj.put("PCD_PAY_OID", params.get("PCD_PAY_OID"));

      log.info("페이플 결제 승인 요청: {}", obj.toString());

      URL url = new URL(confirmUrl);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("content-type", "application/json");
      con.setRequestProperty("charset", "UTF-8");
      con.setRequestProperty("referer", paypleConfig.getRefererUrl());
      con.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.write(obj.toString().getBytes());
      wr.flush();
      wr.close();

      int responseCode = con.getResponseCode();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      jsonObject = (JSONObject) jsonParser.parse(response.toString());

      log.info("페이플 결제 승인 응답: {}", jsonObject.toString());

    } catch (Exception e) {
      log.error("Payple 결제 승인 오류: ", e);
      jsonObject.put("PCD_PAY_RST", "error");
      jsonObject.put("PCD_PAY_MSG", "결제 승인 처리 중 오류가 발생했습니다: " + e.getMessage());
    }

    return jsonObject;
  }

  @Override
  public JSONObject payLinkCreate(Map<String, String> params, Map<String, BigDecimal> prices) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      String linkApiUrl = paypleConfig.getLinkApiUrl();

      JSONObject obj = new JSONObject();
      obj.put("PCD_CST_ID", paypleConfig.getCstId());
      obj.put("PCD_CUST_KEY", paypleConfig.getCustKey());
      obj.put("PCD_AUTH_KEY", paypleConfig.getClientKey());
      obj.put("PCD_PAY_TYPE", "transfer+card");

      // PCD_AUTH_KEY, PCD_PAY_WORK
      if (params != null) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
          obj.put(entry.getKey(), entry.getValue());
        }
      }

      // PCD_PAY_TOTAL
      if (prices != null) {
        for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
          obj.put(entry.getKey(), entry.getValue().toString());
        }
      }

      URL url = new URL(linkApiUrl);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("content-type", "application/json");
      con.setRequestProperty("charset", "UTF-8");
      con.setRequestProperty("referer", "https://groble.im"); // 실제 도메인으로 변경 필요
      con.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.write(obj.toString().getBytes());
      wr.flush();
      wr.close();

      int responseCode = con.getResponseCode();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }

      in.close();

      jsonObject = (JSONObject) jsonParser.parse(response.toString());

    } catch (Exception e) {
      log.error("Payple 링크 결제 오류: ", e);
    }
    return jsonObject;
  }

  @Override
  public JSONObject payAuth(Map<String, String> params) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      String authUrl = paypleConfig.getAuthApiUrl();

      JSONObject obj = new JSONObject();
      obj.put("cst_id", paypleConfig.getCstId());
      obj.put("custKey", paypleConfig.getCustKey());

      // 추가 파라미터 설정
      if (params != null) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
          obj.put(entry.getKey(), entry.getValue());
        }
      }

      URL url = new URL(authUrl);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("content-type", "application/json");
      con.setRequestProperty("charset", "UTF-8");
      con.setRequestProperty("referer", "https://groble.im"); // 실제 도메인으로 변경 필요
      con.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.write(obj.toString().getBytes());
      wr.flush();
      wr.close();

      int responseCode = con.getResponseCode();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }

      in.close();

      jsonObject = (JSONObject) jsonParser.parse(response.toString());

    } catch (Exception e) {
      log.error("Payple 인증 오류: ", e);
    }

    return jsonObject;
  }

  @Override
  public JSONObject payRefund(PaypleRefundRequest request) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      // 결제취소 전 파트너 인증
      Map<String, String> refundParams = new HashMap<>();
      refundParams.put("PCD_PAYCANCEL_FLAG", "Y");

      JSONObject authObj = payAuth(refundParams);

      // 파트너 인증 응답값
      String cstId = (String) authObj.get("cst_id");
      String custKey = (String) authObj.get("custKey");
      String authKey = (String) authObj.get("AuthKey");
      String payRefURL = (String) authObj.get("return_url");

      // 결제취소 요청 전송
      JSONObject refundObj = new JSONObject();
      refundObj.put("PCD_CST_ID", cstId);
      refundObj.put("PCD_CUST_KEY", custKey);
      refundObj.put("PCD_AUTH_KEY", authKey);
      refundObj.put("PCD_REFUND_KEY", paypleConfig.getRefundKey());
      refundObj.put("PCD_PAYCANCEL_FLAG", "Y");
      refundObj.put("PCD_PAY_OID", request.getPayOid());
      refundObj.put("PCD_PAY_DATE", request.getPayDate());
      refundObj.put("PCD_REFUND_TOTAL", request.getRefundTotal());
      if (request.getRefundTaxtotal() != null) {
        refundObj.put("PCD_REFUND_TAXTOTAL", request.getRefundTaxtotal());
      }

      URL url = new URL(payRefURL);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("content-type", "application/json");
      con.setRequestProperty("referer", "https://groble.liaison.com");
      con.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(refundObj.toString());
      wr.flush();
      wr.close();

      int responseCode = con.getResponseCode();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }

      in.close();

      jsonObject = (JSONObject) jsonParser.parse(response.toString());

    } catch (Exception e) {
      log.error("Payple 결제 취소 오류: ", e);
    }

    return jsonObject;
  }
}
