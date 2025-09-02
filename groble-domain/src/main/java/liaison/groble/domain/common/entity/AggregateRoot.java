package liaison.groble.domain.common.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import liaison.groble.domain.common.event.DomainEvent;

/**
 * Aggregate Root 기본 클래스
 *
 * <p>Domain-Driven Design의 Aggregate Root 패턴을 구현합니다. 도메인 이벤트의 발행과 관리를 담당하며, 엔티티의 도메인 이벤트 생명주기를
 * 관리합니다.
 *
 * <p><strong>특징:</strong>
 *
 * <ul>
 *   <li>도메인 이벤트 발행 및 수집
 *   <li>이벤트 클리어 기능
 *   <li>Aggregate 단위의 트랜잭션 보장
 * </ul>
 */
public abstract class AggregateRoot extends BaseTimeEntity {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  /**
   * 도메인 이벤트를 발행합니다.
   *
   * @param event 발행할 도메인 이벤트
   */
  protected void publishEvent(DomainEvent event) {
    if (event != null) {
      this.domainEvents.add(event);
    }
  }

  /**
   * 발행된 모든 도메인 이벤트를 반환합니다.
   *
   * @return 도메인 이벤트 목록 (읽기 전용)
   */
  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  /** 모든 도메인 이벤트를 제거합니다. 일반적으로 이벤트 발행 후 호출됩니다. */
  public void clearDomainEvents() {
    this.domainEvents.clear();
  }

  /**
   * 발행된 도메인 이벤트가 있는지 확인합니다.
   *
   * @return 도메인 이벤트가 있는 경우 true
   */
  public boolean hasDomainEvents() {
    return !domainEvents.isEmpty();
  }
}
