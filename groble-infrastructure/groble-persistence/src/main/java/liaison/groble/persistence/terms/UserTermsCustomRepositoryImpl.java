package liaison.groble.persistence.terms;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.terms.entity.QUserTerms;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserTermsCustomRepositoryImpl implements UserTermsCustomRepository {

  private final JPAQueryFactory queryFactory;

  /**
   * 특정 사용자의 모든 약관 동의 정보를 삭제합니다.
   *
   * @param userId 사용자 ID
   * @return 삭제된 레코드 수
   */
  @Override
  @Transactional
  public long deleteAllByUserId(Long userId) {

    QUserTerms qUserTerms = QUserTerms.userTerms;

    return queryFactory.delete(qUserTerms).where(qUserTerms.user.id.eq(userId)).execute();
  }
}
