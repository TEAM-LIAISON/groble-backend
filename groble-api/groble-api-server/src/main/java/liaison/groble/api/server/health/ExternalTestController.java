package liaison.groble.api.server.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ExternalTestController {

  @GetMapping("/test-external")
  public ResponseEntity<String> testExternalCall() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String body = restTemplate.getForObject("https://www.google.com", String.class);
      return ResponseEntity.ok("✅요청 성공: 응답 길이 = " + body.length());
    } catch (Exception e) {
      log.error("External call failed", e);
      e.printStackTrace();
      return ResponseEntity.status(500).body("요청 실패: " + e.getMessage() + "\n원인: " + e.getCause());
    }
  }
}
