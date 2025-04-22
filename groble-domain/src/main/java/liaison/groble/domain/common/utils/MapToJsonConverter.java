package liaison.groble.domain.common.utils;

import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// JPA 컨버터: Map<String, String>을 JSON으로 변환
@Converter
public class MapToJsonConverter implements AttributeConverter<Map<String, String>, String> {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, String> attribute) {
    try {
      return attribute == null ? null : objectMapper.writeValueAsString(attribute);
    } catch (Exception e) {
      throw new RuntimeException("JSON 변환 오류", e);
    }
  }

  @Override
  public Map<String, String> convertToEntityAttribute(String dbData) {
    try {
      return dbData == null
          ? null
          : objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      throw new RuntimeException("JSON 파싱 오류", e);
    }
  }
}
