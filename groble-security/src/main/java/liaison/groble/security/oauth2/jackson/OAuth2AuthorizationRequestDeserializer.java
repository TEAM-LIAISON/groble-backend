package liaison.groble.security.oauth2.jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OAuth2AuthorizationRequest 객체를 위한 커스텀 JSON 역직렬화 클래스 Spring Security의 OAuth2AuthorizationRequest
 * 클래스를 Jackson에서 안전하게 역직렬화하기 위한 로직 제공
 */
public class OAuth2AuthorizationRequestDeserializer
    extends JsonDeserializer<OAuth2AuthorizationRequest> {

  @Override
  public OAuth2AuthorizationRequest deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    ObjectMapper mapper = (ObjectMapper) parser.getCodec();
    JsonNode root = mapper.readTree(parser);

    // 1. 필수 필드 파싱
    String authorizationUri = root.path("authorizationUri").asText();
    String clientId = root.path("clientId").asText();

    // 2. OAuth2AuthorizationResponseType 처리 (enum 값을 직접 생성)
    OAuth2AuthorizationResponseType responseType;
    String responseTypeValue = root.path("responseType").path("value").asText();
    if ("code".equals(responseTypeValue)) {
      responseType = OAuth2AuthorizationResponseType.CODE;
    } else {
      // 기본값 또는 다른 타입 처리
      responseType = OAuth2AuthorizationResponseType.CODE;
    }

    // 3. 리다이렉트 URI
    String redirectUri = root.path("redirectUri").asText(null);

    // 4. Scopes 처리
    Set<String> scopes = new HashSet<>();
    JsonNode scopesNode = root.path("scopes");
    if (scopesNode.isArray()) {
      for (JsonNode scope : scopesNode) {
        scopes.add(scope.asText());
      }
    }

    // 5. State
    String state = root.path("state").asText(null);

    // 6. Additional parameters
    Map<String, Object> additionalParameters = new HashMap<>();
    JsonNode additionalParamsNode = root.path("additionalParameters");
    if (!additionalParamsNode.isMissingNode() && additionalParamsNode.isObject()) {
      Iterator<String> fieldNames = additionalParamsNode.fieldNames();
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        JsonNode valueNode = additionalParamsNode.get(fieldName);
        // String 값만 처리 (복잡한 객체는 현재 지원하지 않음)
        if (valueNode.isTextual()) {
          additionalParameters.put(fieldName, valueNode.asText());
        } else if (valueNode.isNumber()) {
          additionalParameters.put(fieldName, valueNode.asInt());
        } else if (valueNode.isBoolean()) {
          additionalParameters.put(fieldName, valueNode.asBoolean());
        }
      }
    }

    // 7. AuthorizationRequestAttributes
    Map<String, Object> attributes = new HashMap<>();
    JsonNode attributesNode = root.path("attributes");
    if (!attributesNode.isMissingNode() && attributesNode.isObject()) {
      Iterator<String> fieldNames = attributesNode.fieldNames();
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        JsonNode valueNode = attributesNode.get(fieldName);
        // String 값만 처리 (복잡한 객체는 현재 지원하지 않음)
        if (valueNode.isTextual()) {
          attributes.put(fieldName, valueNode.asText());
        } else if (valueNode.isNumber()) {
          attributes.put(fieldName, valueNode.asInt());
        } else if (valueNode.isBoolean()) {
          attributes.put(fieldName, valueNode.asBoolean());
        }
      }
    }

    // 8. AuthorizationGrantType 처리
    AuthorizationGrantType authorizationGrantType = null;
    JsonNode grantTypeNode = root.path("authorizationGrantType");
    if (!grantTypeNode.isMissingNode()) {
      String grantTypeValue = grantTypeNode.path("value").asText();
      if ("authorization_code".equals(grantTypeValue)) {
        authorizationGrantType = AuthorizationGrantType.AUTHORIZATION_CODE;
      } else if ("refresh_token".equals(grantTypeValue)) {
        authorizationGrantType = AuthorizationGrantType.REFRESH_TOKEN;
      } else if ("client_credentials".equals(grantTypeValue)) {
        authorizationGrantType = AuthorizationGrantType.CLIENT_CREDENTIALS;
      } else if ("password".equals(grantTypeValue)) {
        authorizationGrantType = AuthorizationGrantType.PASSWORD;
      } else {
        // 알 수 없는 경우 기본값
        authorizationGrantType = AuthorizationGrantType.AUTHORIZATION_CODE;
      }
    } else {
      // 기본값
      authorizationGrantType = AuthorizationGrantType.AUTHORIZATION_CODE;
    }

    // 9. OAuth2AuthorizationRequest.Builder를 사용하여 객체 생성
    OAuth2AuthorizationRequest.Builder builder =
        OAuth2AuthorizationRequest.authorizationCode()
            .clientId(clientId)
            .authorizationUri(authorizationUri)
            .redirectUri(redirectUri)
            .scopes(scopes);

    // 10. 선택적 필드 설정
    if (state != null) {
      builder.state(state);
    }

    if (!additionalParameters.isEmpty()) {
      builder.additionalParameters(additionalParameters);
    }

    if (!attributes.isEmpty()) {
      builder.attributes(attrs -> attrs.putAll(attributes));
    }

    // 11. 최종 객체 생성
    return builder.build();
  }
}
