package liaison.groble.persistence.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.terms.entity.QTerms;
import liaison.groble.domain.terms.entity.QUserTerms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.dto.FlatAdminUserSummaryInfoDTO;
import liaison.groble.domain.user.entity.QIntegratedAccount;
import liaison.groble.domain.user.entity.QSocialAccount;
import liaison.groble.domain.user.entity.QUser;
import liaison.groble.domain.user.repository.UserCustomRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<FlatAdminUserSummaryInfoDTO> findUsersByPageable(Pageable pageable) {
    QUser qUser = QUser.user;
    QIntegratedAccount qIntegratedAccount = QIntegratedAccount.integratedAccount;
    QSocialAccount qSocialAccount = QSocialAccount.socialAccount;
    QUserTerms qMarketingTerms = new QUserTerms("marketingTerms");
    QUserTerms qSellerTerms = new QUserTerms("sellerTerms");
    QTerms qMarketingTermsEntity = new QTerms("marketingTermsEntity");
    QTerms qSellerTermsEntity = new QTerms("sellerTermsEntity");

    // 한 번의 쿼리로 모든 데이터 조회
    JPAQuery<FlatAdminUserSummaryInfoDTO> query =
        queryFactory
            .select(
                Projections.constructor(
                    FlatAdminUserSummaryInfoDTO.class,
                    qUser.createdAt.as("createdAt"),
                    qSellerTerms.agreed.coalesce(false).as("isSellerTermsAgreed"),
                    qUser.userProfile.nickname.as("nickname"),
                    qIntegratedAccount
                        .integratedAccountEmail
                        .coalesce(qSocialAccount.socialAccountEmail)
                        .as("email"),
                    qUser.userProfile.phoneNumber.as("phoneNumber"),
                    qMarketingTerms.agreed.coalesce(false).as("isMarketingAgreed"),
                    qUser
                        .sellerInfo
                        .businessLicenseFileUrl
                        .isNotNull()
                        .and(qUser.sellerInfo.businessLicenseFileUrl.length().gt(0)),
                    qUser.sellerInfo.verificationStatus.stringValue().coalesce("NONE"),
                    qUser.sellerInfo.isBusinessSeller.coalesce(false).as("isBusinessSeller")))
            .from(qUser)
            .leftJoin(qUser.integratedAccount, qIntegratedAccount)
            .leftJoin(qUser.socialAccount, qSocialAccount)
            .leftJoin(qUser.termsAgreements, qMarketingTerms)
            .on(
                qMarketingTerms
                    .terms
                    .type
                    .eq(TermsType.MARKETING_POLICY)
                    .and(qMarketingTerms.agreed.isTrue()))
            .leftJoin(qUser.termsAgreements, qSellerTerms)
            .on(
                qSellerTerms
                    .terms
                    .type
                    .eq(TermsType.SELLER_TERMS_POLICY)
                    .and(qSellerTerms.agreed.isTrue()))
            .orderBy(qUser.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    List<FlatAdminUserSummaryInfoDTO> content = query.fetch();

    // 전체 카운트 조회
    Long total = queryFactory.select(qUser.count()).from(qUser).fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0);
  }
}
