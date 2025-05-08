package liaison.groble.domain.scrap.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 엔티티 통합계정, 소셜계정 등 모든 유형의 계정에 공통으로 사용되는 사용자 정보를 관리 */
@Entity
@Table(
    name = "scraps",
    indexes = {@Index(name = "idx_scrap_user_content", columnList = "user_id, content_id")})
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class ContentScrap extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;
}
