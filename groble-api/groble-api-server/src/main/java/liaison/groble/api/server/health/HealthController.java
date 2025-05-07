package liaison.groble.api.server.health;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.common.response.ApiResponse;

@RestController
@RequestMapping
public class HealthController {

  @Value("${server.env}")
  private String env;

  @GetMapping("/hc")
  public ResponseEntity<ApiResponse<Map<String, String>>> healthcheck() {
    Map<String, String> responseData = new TreeMap<>();
    responseData.put("env", env);

    return ResponseEntity.ok(ApiResponse.success(responseData));
  }

  @GetMapping("/env")
  public ResponseEntity<?> getEnv() {

    return ResponseEntity.ok(env);
  }
}
