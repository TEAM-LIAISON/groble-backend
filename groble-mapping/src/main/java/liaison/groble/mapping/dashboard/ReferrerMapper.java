package liaison.groble.mapping.dashboard;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.dashboard.request.referrer.ReferrerRequest;
import liaison.groble.application.dashboard.dto.referrer.ReferrerDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface ReferrerMapper {

  ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

  @Mapping(
      target = "timestamp",
      expression = "java(toLocalDateTime(referrerRequest.getTimestamp()))")
  ReferrerDTO toContentReferrerDTO(ReferrerRequest referrerRequest);

  default LocalDateTime toLocalDateTime(String timestamp) {
    if (timestamp == null || timestamp.isBlank()) {
      return LocalDateTime.now(ASIA_SEOUL);
    }
    try {
      return OffsetDateTime.parse(timestamp).atZoneSameInstant(ASIA_SEOUL).toLocalDateTime();
    } catch (DateTimeParseException e) {
      return LocalDateTime.now(ASIA_SEOUL);
    }
  }
}
