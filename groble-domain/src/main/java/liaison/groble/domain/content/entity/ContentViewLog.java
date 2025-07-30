package liaison.groble.domain.content.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 콘텐츠 조회 로그를 관리하는 엔티티
 *
 * @author 권동민
 * @since 2025-07-28
 */
@Entity
@Table(name = "content_view_logs")
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class ContentViewLog {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  // 조회한 콘텐츠 ID
  @Column(name = "content_id", nullable = false)
  private Long contentId;

  /**
   * 조회한 사용자 ID
   *
   * <p>- 로그인 사용자: User 엔티티의 ID
   *
   * <p>- 비로그인 사용자: null
   */
  @Column(name = "viewer_id")
  private Long viewerId;

  /**
   * 조회자 IP 주소
   *
   * <p>- 비로그인 사용자 식별에 사용
   *
   * <p>- IPv6 지원 (최대 45자)
   */
  @Column(name = "viewer_ip", length = 45)
  private String viewerIp;

  /**
   * 브라우저 User-Agent
   *
   * <p>- 비로그인 사용자의 경우 IP와 함께 유니크 사용자 판별
   *
   * <p>- 동일 IP에서 다른 브라우저로 접속 시 구분 가능
   */
  @Column(name = "user_agent", length = 500)
  private String userAgent;

  /**
   * 유입 경로 (이전 페이지 URL)
   *
   * <p>- 트래픽 분석용
   */
  @Column(name = "referer", length = 500)
  private String referer;

  /**
   * 조회 시각
   *
   * <p>- 자동 생성되며 수정 불가
   */
  @CreatedDate
  @Column(name = "viewed_at", nullable = false, updatable = false)
  private LocalDateTime viewedAt;
}
