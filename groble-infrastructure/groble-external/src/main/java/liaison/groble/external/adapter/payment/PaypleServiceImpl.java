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
  public JSONObject payLinkCreate(Map<String, String> params, Map<String, BigDecimal> amounts) {
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
      if (amounts != null) {
        for (Map.Entry<String, BigDecimal> entry : amounts.entrySet()) {
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

  @Override
  public JSONObject payInfo(PayplePayInfoRequest request) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      // 결제결과 조회 전 파트너 인증
      Map<String, String> infoParams = new HashMap<>();
      infoParams.put("PCD_PAYCHK_FLAG", "Y");

      JSONObject authObj = payAuth(infoParams);

      // 파트너 인증 응답값
      String cstId = (String) authObj.get("cst_id");
      String custKey = (String) authObj.get("custKey");
      String authKey = (String) authObj.get("AuthKey");
      String payInfoURL = (String) authObj.get("return_url");

      // 결제결과 조회 요청 전송
      JSONObject payInfoObj = new JSONObject();
      payInfoObj.put("PCD_CST_ID", cstId);
      payInfoObj.put("PCD_CUST_KEY", custKey);
      payInfoObj.put("PCD_AUTH_KEY", authKey);
      payInfoObj.put("PCD_PAYCHK_FLAG", "Y");
      payInfoObj.put("PCD_PAY_TYPE", request.getPayType());
      payInfoObj.put("PCD_PAY_OID", request.getPayOid());
      payInfoObj.put("PCD_PAY_DATE", request.getPayDate());

      URL url = new URL(payInfoURL);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("content-type", "application/json");
      con.setRequestProperty("referer", "https://groble.liaison.com");
      con.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(payInfoObj.toString());
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
      log.error("Payple 결제 정보 조회 오류: ", e);
    }

    return jsonObject;
  }

  @Override
  public JSONObject paySimple(PaypleSimplePayRequest request) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      // 정기결제 재결제 전 파트너 인증
      Map<String, String> billingParams = new HashMap<>();
      billingParams.put("PCD_PAY_TYPE", request.getPayType());
      billingParams.put("PCD_SIMPLE_FLAG", "Y");

      JSONObject authObj = payAuth(billingParams);

      // 파트너 인증 응답값
      String cstId = (String) authObj.get("cst_id");
      String custKey = (String) authObj.get("custKey");
      String authKey = (String) authObj.get("AuthKey");
      String billingURL = (String) authObj.get("return_url");

      // 정기결제 재결제 요청 전송
      JSONObject billingObj = new JSONObject();
      billingObj.put("PCD_CST_ID", cstId);
      billingObj.put("PCD_CUST_KEY", custKey);
      billingObj.put("PCD_AUTH_KEY", authKey);
      billingObj.put("PCD_PAY_TYPE", request.getPayType());
      billingObj.put("PCD_PAYER_ID", request.getPayerId());
      billingObj.put("PCD_PAY_GOODS", request.getPayGoods());
      billingObj.put("PCD_SIMPLE_FLAG", "Y");
      billingObj.put("PCD_PAY_TOTAL", request.getPayTotal());
      billingObj.put("PCD_PAY_OID", request.getPayOid());
      billingObj.put("PCD_PAYER_NO", request.getPayerNo());
      billingObj.put("PCD_PAYER_NAME", request.getPayerName());
      billingObj.put("PCD_PAYER_HP", request.getPayerHp());
      billingObj.put("PCD_PAYER_EMAIL", request.getPayerEmail());
      billingObj.put("PCD_PAY_ISTAX", request.getPayIstax());
      billingObj.put("PCD_PAY_TAXTOTAL", request.getPayTaxtotal());

      URL url = new URL(billingURL);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("content-type", "application/json");
      con.setRequestProperty("charset", "UTF-8");
      con.setRequestProperty("referer", "https://groble.liaison.com");
      con.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.write(billingObj.toString().getBytes());
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
      log.error("Payple 빌링 결제 오류: ", e);
    }

    return jsonObject;
  }

  @Override
  public JSONObject payUserInfo(String payerId) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      // 파트너 인증
      Map<String, String> params = new HashMap<>();
      params.put("PCD_PAY_WORK", "PUSERINFO");

      JSONObject authObj = payAuth(params);

      // 파트너 인증 응답값
      String cstId = (String) authObj.get("cst_id");
      String custKey = (String) authObj.get("custKey");
      String authKey = (String) authObj.get("AuthKey");
      String payUserInfoURL = (String) authObj.get("return_url");

      // 요청 전송
      JSONObject payUserInfoObj = new JSONObject();
      payUserInfoObj.put("PCD_CST_ID", cstId);
      payUserInfoObj.put("PCD_CUST_KEY", custKey);
      payUserInfoObj.put("PCD_AUTH_KEY", authKey);
      payUserInfoObj.put("PCD_PAYER_ID", payerId);

      URL url = new URL(payUserInfoURL);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("content-type", "application/json");
      con.setRequestProperty("referer", "https://groble.liaison.com");
      con.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.write(payUserInfoObj.toString().getBytes());
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
      log.error("Payple 사용자 정보 조회 오류: ", e);
    }

    return jsonObject;
  }

  @Override
  public JSONObject payUserDel(String payerId) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      // 파트너 인증
      Map<String, String> params = new HashMap<>();
      params.put("PCD_PAY_WORK", "PUSERDEL");

      JSONObject authObj = payAuth(params);

      // 파트너 인증 응답값
      String cstId = (String) authObj.get("cst_id");
      String custKey = (String) authObj.get("custKey");
      String authKey = (String) authObj.get("AuthKey");
      String payUserDelURL = (String) authObj.get("return_url");

      // 요청 전송
      JSONObject payUserDelObj = new JSONObject();
      payUserDelObj.put("PCD_CST_ID", cstId);
      payUserDelObj.put("PCD_CUST_KEY", custKey);
      payUserDelObj.put("PCD_AUTH_KEY", authKey);
      payUserDelObj.put("PCD_PAYER_ID", payerId);

      URL url = new URL(payUserDelURL);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("content-type", "application/json");
      con.setRequestProperty("referer", "https://groble.liaison.com");
      con.setDoOutput(true);

      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.write(payUserDelObj.toString().getBytes());
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
      log.error("Payple 사용자 해지 오류: ", e);
    }

    return jsonObject;
  }
}
