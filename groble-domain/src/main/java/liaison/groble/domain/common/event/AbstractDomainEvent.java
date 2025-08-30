package liaison.groble.domain.common.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 도메인 이벤트 추상 클래스
 *
 * <p>Template Method Pattern을 적용하여 모든 도메인 이벤트의 공통 기능을 제공하는 기본 구현체입니다.
 *
 * <p><strong>제공 기능:</strong>
 *
 * <ul>
 *   <li>자동 이벤트 ID 생성
 *   <li>자동 발생 시각 기록
 *   <li>이벤트 타입 자동 추출
 *   <li>equals/hashCode 구현
 * </ul>
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDomainEvent implements DomainEvent {

  private final UUID eventId;
  private final LocalDateTime occurredAt;
  private final String aggregateId;

  protected AbstractDomainEvent(String aggregateId) {
    this.eventId = UUID.randomUUID();
    this.occurredAt = LocalDateTime.now();
    this.aggregateId = aggregateId;
  }

  @Override
  public UUID getEventId() {
    return eventId;
  }

  @Override
  public LocalDateTime getOccurredAt() {
    return occurredAt;
  }

  @Override
  public String getAggregateId() {
    return aggregateId;
  }

  @Override
  public String getEventType() {
    return this.getClass().getSimpleName();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    AbstractDomainEvent that = (AbstractDomainEvent) obj;
    return Objects.equals(eventId, that.eventId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId);
  }

  @Override
  public String toString() {
    return String.format(
        "%s{eventId=%s, aggregateId='%s', occurredAt=%s}",
        getEventType(), eventId, aggregateId, occurredAt);
  }
}
