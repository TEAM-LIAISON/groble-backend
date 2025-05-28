package liaison.groble.external.sms;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.external.config.SmsConfig;
import liaison.groble.external.sms.exception.SmsSendException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsSender {
  private final SmsConfig smsConfig;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public SmsResponse sendSms(Message message) {
    long timestamp = System.currentTimeMillis();
    String configuredFrom = smsConfig.getSendFrom();
    log.debug("▶ sendSms 시작: configuredFrom={}, to={}", configuredFrom, message.getTo());

    try {
      HttpHeaders headers = createHeaders(timestamp);
      SmsRequest smsRequest =
          SmsRequest.builder()
              .type("SMS")
              .contentType("COMM")
              .countryCode("82")
              .from(configuredFrom) // 여기에 찍힌 번호가 SENS에 등록된 발신번호여야 합니다
              .content(message.getContent())
              .messages(List.of(Message.builder().to(message.getTo()).build()))
              .build();

      String body = objectMapper.writeValueAsString(smsRequest);
      log.debug("생성된 SMS 요청 바디: {}", body);

      URI uri =
          new URI(
              "https://sens.apigw.ntruss.com/sms/v2/services/"
                  + smsConfig.getServiceId()
                  + "/messages");
      SmsResponse res =
          restTemplate.postForObject(uri, new HttpEntity<>(body, headers), SmsResponse.class);

      // 응답 체크...
      return res;
    } catch (Exception e) {
      log.error("sendSms 예외 발생: {}", e.getMessage(), e);
      throw new SmsSendException();
    }
  }

  private HttpHeaders createHeaders(long timestamp)
      throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
    String sig = makeSignature(timestamp);
    log.debug("생성된 Signature: {}", sig);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("x-ncp-apigw-timestamp", String.valueOf(timestamp));
    headers.set("x-ncp-iam-access-key", smsConfig.getAccessKey());
    headers.set("x-ncp-apigw-signature-v2", sig);
    return headers;
  }

  public String makeSignature(Long time)
      throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
    String space = " ";
    String newLine = "\n";
    String method = "POST";
    String url = "/sms/v2/services/" + smsConfig.getServiceId() + "/messages";
    String timestamp = time.toString();
    String accessKey = smsConfig.getAccessKey();
    String secretKey = smsConfig.getSecretKey();

    String message =
        new StringBuilder()
            .append(method)
            .append(space)
            .append(url)
            .append(newLine)
            .append(timestamp)
            .append(newLine)
            .append(accessKey)
            .toString();

    SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(signingKey);

    byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
    String encodeBase64String = Base64.encodeBase64String(rawHmac);

    return encodeBase64String;
  }
}
