package liaison.groble.external.adapter.payment;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
      String appCardPaymentUrl = params.get("PCD_PAY_COFURL");
      log.info(appCardPaymentUrl);

      // 요청 파라미터 구성
      JSONObject obj = new JSONObject();
      obj.put("PCD_CST_ID", params.get("PCD_CST_ID"));
      obj.put("PCD_CUST_KEY", params.get("PCD_CUST_KEY"));
      obj.put("PCD_AUTH_KEY", params.get("PCD_AUTH_KEY"));
      obj.put("PCD_PAY_REQKEY", params.get("PCD_PAY_REQKEY"));

      log.info("페이플 앱카드 승인 요청: {}", obj.toString());

      URL url = new URL(appCardPaymentUrl);
      log.info("페이플 앱카드 결제 요청 URL: {}", url.toString());
      // 또는 요청 전 로깅 추가
      log.info("PCD_PAY_REQKEY 길이: {}", params.get("PCD_PAY_REQKEY").length());
      log.info("PCD_PAY_REQKEY 값: {}", params.get("PCD_PAY_REQKEY"));
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
      // 페이플 취소 API URL (고정)
      String cancelUrl = paypleConfig.getCancelApiUrl();

      log.info("페이플 결제 취소 요청 시작 - 주문번호: {}, 금액: {}", request.getPayOid(), request.getRefundTotal());

      // 결제취소 요청 파라미터 구성
      JSONObject refundObj = new JSONObject();
      refundObj.put("PCD_CST_ID", paypleConfig.getCstId());
      refundObj.put("PCD_CUST_KEY", paypleConfig.getCustKey());
      refundObj.put("PCD_AUTH_KEY", request.getAuthKey());
      refundObj.put("PCD_REFUND_KEY", paypleConfig.getRefundKey());
      refundObj.put("PCD_PAYCANCEL_FLAG", "Y");
      refundObj.put("PCD_PAY_OID", request.getPayOid());
      refundObj.put("PCD_PAY_DATE", request.getPayDate());
      refundObj.put("PCD_REFUND_TOTAL", request.getRefundTotal());

      if (request.getRefundTaxtotal() != null) {
        refundObj.put("PCD_REFUND_TAXTOTAL", request.getRefundTaxtotal());
      }

      log.debug("페이플 결제 취소 요청 파라미터: {}", refundObj.toString());

      // HTTP 연결 설정
      URL url = new URL(cancelUrl);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Cache-Control", "no-cache");
      con.setRequestProperty("Referer", paypleConfig.getRefererUrl());
      con.setDoOutput(true);

      // 요청 전송
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.write(refundObj.toString().getBytes("UTF-8"));
      wr.flush();
      wr.close();

      // 응답 처리
      int responseCode = con.getResponseCode();
      log.debug("페이플 취소 응답 코드: {}", responseCode);

      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      jsonObject = (JSONObject) jsonParser.parse(response.toString());

      log.info("페이플 결제 취소 응답: {}", jsonObject.toString());

    } catch (Exception e) {
      log.error("페이플 결제 취소 중 오류 발생 - 주문번호: {}", request.getPayOid(), e);
      jsonObject.put("PCD_PAY_RST", "error");
      jsonObject.put("PCD_PAY_MSG", "결제 취소 처리 중 오류가 발생했습니다: " + e.getMessage());
    }

    return jsonObject;
  }

  @Override
  public JSONObject paySimplePayment(Map<String, String> params) {
    JSONObject jsonObject = new JSONObject();
    JSONParser jsonParser = new JSONParser();

    try {
      // 빌링(간편) 결제 URL
      String simplePaymentUrl = paypleConfig.getSimplePaymentUrl();

      // 요청 파라미터 구성
      JSONObject obj = new JSONObject();
      obj.put("PCD_CST_ID", params.get("PCD_CST_ID"));
      obj.put("PCD_CUST_KEY", params.get("PCD_CUST_KEY"));
      obj.put("PCD_AUTH_KEY", params.get("PCD_AUTH_KEY"));
      obj.put("PCD_PAY_TYPE", params.get("PCD_PAY_TYPE"));
      obj.put("PCD_PAYER_ID", params.get("PCD_PAYER_ID"));
      obj.put("PCD_PAY_GOODS", params.get("PCD_PAY_GOODS"));
      obj.put("PCD_PAY_TOTAL", params.get("PCD_PAY_TOTAL"));
      obj.put("PCD_SIMPLE_FLAG", params.get("PCD_SIMPLE_FLAG"));
      obj.put("PCD_PAY_OID", params.get("PCD_PAY_OID"));
      obj.put("PCD_PAYER_NO", params.get("PCD_PAYER_NO"));
      obj.put("PCD_PAYER_NAME", params.get("PCD_PAYER_NAME"));
      obj.put("PCD_PAYER_HP", params.get("PCD_PAYER_HP"));
      obj.put("PCD_PAYER_EMAIL", params.get("PCD_PAYER_EMAIL"));

      log.info("페이플 빌링 결제 요청: {}", obj.toString());

      URL url = new URL(simplePaymentUrl);
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

      log.info("페이플 빌링 결제 응답: {}", jsonObject.toString());

    } catch (Exception e) {
      log.error("Payple 빌링 결제 오류: ", e);
      jsonObject.put("PCD_PAY_RST", "error");
      jsonObject.put("PCD_PAY_MSG", "빌링 결제 처리 중 오류가 발생했습니다: " + e.getMessage());
    }

    return jsonObject;
  }
}
