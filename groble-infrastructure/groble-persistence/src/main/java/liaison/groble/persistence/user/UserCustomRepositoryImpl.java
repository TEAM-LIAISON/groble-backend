package liaison.groble.persistence.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.terms.entity.QUserTerms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.dto.FlatAdminUserSummaryInfoDTO;
import liaison.groble.domain.user.entity.QIntegratedAccount;
import liaison.groble.domain.user.entity.QSellerInfo;
import liaison.groble.domain.user.entity.QSocialAccount;
import liaison.groble.domain.user.entity.QUser;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.UserCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserCustomRepositoryImpl implements UserCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public boolean existsByIntegratedAccountEmailAndPhoneNumber(String integratedAccountEmail) {
    QUser qUser = QUser.user;
    QIntegratedAccount qIntegratedAccount = QIntegratedAccount.integratedAccount;

    return queryFactory
            .selectOne()
            .from(qUser)
            .leftJoin(qUser.integratedAccount, qIntegratedAccount)
            .where(
                qIntegratedAccount
                    .integratedAccountEmail
                    .eq(integratedAccountEmail)
                    .and(qUser.userProfile.isNotNull())
                    .and(qUser.userProfile.phoneNumber.isNotNull())
                    .and(qUser.userStatusInfo.isNotNull())
                    .and(qUser.userStatusInfo.status.eq(UserStatus.ACTIVE)))
            .fetchFirst()
        != null;
  }

  @Override
  public Page<FlatAdminUserSummaryInfoDTO> findUsersByPageable(Pageable pageable) {
    QUser qUser = QUser.user;
    QIntegratedAccount qInt = QIntegratedAccount.integratedAccount;
    QSocialAccount qSoc = QSocialAccount.socialAccount;
    QUserTerms ut = QUserTerms.userTerms;
    QSellerInfo qSellerInfo = QSellerInfo.sellerInfo;

    // 마케팅 수신 동의 존재 여부
    BooleanExpression marketingAgreedExists =
        JPAExpressions.selectOne()
            .from(ut)
            .where(
                ut.user.eq(qUser), ut.terms.type.eq(TermsType.MARKETING_POLICY), ut.agreed.isTrue())
            .exists();

    // 판매자 약관 동의 존재 여부
    BooleanExpression sellerAgreedExists =
        JPAExpressions.selectOne()
            .from(ut)
            .where(
                ut.user.eq(qUser),
                ut.terms.type.eq(TermsType.SELLER_TERMS_POLICY),
                ut.agreed.isTrue())
            .exists();

    JPAQuery<FlatAdminUserSummaryInfoDTO> query =
        queryFactory
            .select(
                Projections.constructor(
                    FlatAdminUserSummaryInfoDTO.class,
                    qUser.createdAt.as("createdAt"),
                    sellerAgreedExists.as("isSellerTermsAgreed"),
                    qUser.userProfile.nickname.as("nickname"),
                    qInt.integratedAccountEmail.coalesce(qSoc.socialAccountEmail),
                    qUser.userProfile.phoneNumber.as("phoneNumber"),
                    marketingAgreedExists.as("isMarketingAgreed"),
                    qSellerInfo
                        .businessLicenseFileUrl
                        .isNotNull()
                        .and(qSellerInfo.businessLicenseFileUrl.length().gt(0))
                        .as("hasBusinessLicense"),
                    qSellerInfo
                        .verificationStatus
                        .stringValue()
                        .coalesce("NONE")
                        .as("verificationStatus"),
                    qSellerInfo.businessSellerRequest.coalesce(false).as("isBusinessSeller"),
                    qSellerInfo.businessType.stringValue().coalesce("NONE").as("businessType")))
            .from(qUser)
            .leftJoin(qUser.integratedAccount, qInt)
            .leftJoin(qUser.socialAccount, qSoc)
            .leftJoin(qSellerInfo)
            .on(qSellerInfo.user.eq(qUser))
            .orderBy(qUser.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    List<FlatAdminUserSummaryInfoDTO> content = query.fetch();
    Long total = queryFactory.select(qUser.count()).from(qUser).fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0);
  }
}
