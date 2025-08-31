package liaison.groble.domain.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 이벤트 기본 인터페이스
 *
 * <p>Domain-Driven Design의 Domain Event 패턴을 구현합니다. 모든 도메인 이벤트는 이 인터페이스를 구현해야 하며, 이벤트의 식별자와 발생 시각을
 * 필수로 가집니다.
 *
 * <p><strong>특징:</strong>
 *
 * <ul>
 *   <li>불변성: 이벤트는 생성 후 변경될 수 없음
 *   <li>식별성: 각 이벤트는 고유한 식별자를 가짐
 *   <li>시간성: 이벤트 발생 시각을 기록
 * </ul>
 */
public interface DomainEvent {

  /**
   * 이벤트의 고유 식별자를 반환합니다.
   *
   * @return 이벤트 ID
   */
  UUID getEventId();

  /**
   * 이벤트가 발생한 시각을 반환합니다.
   *
   * @return 발생 시각
   */
  LocalDateTime getOccurredAt();

  /**
   * 이벤트 타입을 반환합니다.
   *
   * @return 이벤트 타입 문자열
   */
  String getEventType();

  /**
   * 이벤트와 관련된 Aggregate의 ID를 반환합니다.
   *
   * @return Aggregate ID
   */
  String getAggregateId();
}
