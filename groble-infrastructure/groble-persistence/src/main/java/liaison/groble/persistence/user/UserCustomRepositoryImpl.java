package liaison.groble.persistence.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.terms.entity.QUserTerms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.dto.AdminUserStatisticsAggregate;
import liaison.groble.domain.user.dto.FlatAdminUserSummaryInfoDTO;
import liaison.groble.domain.user.entity.QIntegratedAccount;
import liaison.groble.domain.user.entity.QSellerInfo;
import liaison.groble.domain.user.entity.QSocialAccount;
import liaison.groble.domain.user.entity.QUser;
import liaison.groble.domain.user.entity.QUserWithdrawalHistory;
import liaison.groble.domain.user.enums.BusinessType;
import liaison.groble.domain.user.enums.SellerVerificationStatus;
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
    return fetchAdminUserPage(pageable, null);
  }

  @Override
  public Optional<FlatAdminUserSummaryInfoDTO> findUserByNickname(String nickname) {
    BooleanExpression predicate = QUser.user.userProfile.nickname.eq(nickname);
    return Optional.ofNullable(createAdminUserQuery(predicate).fetchFirst());
  }

  @Override
  public AdminUserStatisticsAggregate fetchUserStatistics(
      LocalDateTime sevenDaysAgo, LocalDateTime thirtyDaysAgo) {
    QUser qUser = QUser.user;
    QSellerInfo qSellerInfo = QSellerInfo.sellerInfo;
    QUserTerms qUserTerms = QUserTerms.userTerms;

    BooleanExpression isActive = qUser.userStatusInfo.status.eq(UserStatus.ACTIVE);

    BooleanExpression marketingAgreedExists =
        JPAExpressions.selectOne()
            .from(qUserTerms)
            .where(
                qUserTerms.user.eq(qUser),
                qUserTerms.terms.type.eq(TermsType.MARKETING_POLICY),
                qUserTerms.agreed.isTrue())
            .exists();

    BooleanExpression sellerTermsAgreedExists =
        JPAExpressions.selectOne()
            .from(qUserTerms)
            .where(
                qUserTerms.user.eq(qUser),
                qUserTerms.terms.type.eq(TermsType.SELLER_TERMS_POLICY),
                qUserTerms.agreed.isTrue())
            .exists();

    BooleanExpression phoneProvided =
        qUser.userProfile.phoneNumber.isNotNull().and(qUser.userProfile.phoneNumber.ne(""));

    BooleanExpression hasSellerInfo = qSellerInfo.id.isNotNull();
    BooleanExpression lacksSellerInfo = qSellerInfo.id.isNull();
    BooleanExpression businessTypeIsNull = qSellerInfo.businessType.isNull();

    NumberExpression<Long> totalActiveExpr =
        new CaseBuilder().when(isActive).then(1L).otherwise(0L);
    NumberExpression<Long> withdrawnExpr =
        new CaseBuilder()
            .when(qUser.userStatusInfo.status.eq(UserStatus.WITHDRAWN))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> newUsers7Expr =
        new CaseBuilder()
            .when(isActive.and(qUser.createdAt.goe(sevenDaysAgo)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> newUsers30Expr =
        new CaseBuilder()
            .when(isActive.and(qUser.createdAt.goe(thirtyDaysAgo)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> buyerOnlyExpr =
        new CaseBuilder().when(isActive.and(lacksSellerInfo)).then(1L).otherwise(0L);
    NumberExpression<Long> buyerAndSellerExpr =
        new CaseBuilder().when(isActive.and(hasSellerInfo)).then(1L).otherwise(0L);
    NumberExpression<Long> marketingAgreedExpr =
        new CaseBuilder().when(isActive.and(marketingAgreedExists)).then(1L).otherwise(0L);
    NumberExpression<Long> phoneProvidedExpr =
        new CaseBuilder().when(isActive.and(phoneProvided)).then(1L).otherwise(0L);
    NumberExpression<Long> sellerTermsAgreedExpr =
        new CaseBuilder().when(isActive.and(sellerTermsAgreedExists)).then(1L).otherwise(0L);
    NumberExpression<Long> verificationVerifiedExpr =
        new CaseBuilder()
            .when(
                isActive.and(qSellerInfo.verificationStatus.eq(SellerVerificationStatus.VERIFIED)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> verificationPendingExpr =
        new CaseBuilder()
            .when(isActive.and(qSellerInfo.verificationStatus.eq(SellerVerificationStatus.PENDING)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> verificationInProgressExpr =
        new CaseBuilder()
            .when(
                isActive.and(
                    qSellerInfo.verificationStatus.eq(SellerVerificationStatus.IN_PROGRESS)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> verificationFailedExpr =
        new CaseBuilder()
            .when(isActive.and(qSellerInfo.verificationStatus.eq(SellerVerificationStatus.FAILED)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> verificationNoneExpr =
        new CaseBuilder()
            .when(isActive.and(qSellerInfo.id.isNull().or(qSellerInfo.verificationStatus.isNull())))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> businessTypeSimplifiedExpr =
        new CaseBuilder()
            .when(isActive.and(qSellerInfo.businessType.eq(BusinessType.INDIVIDUAL_SIMPLIFIED)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> businessTypeNormalExpr =
        new CaseBuilder()
            .when(isActive.and(qSellerInfo.businessType.eq(BusinessType.INDIVIDUAL_NORMAL)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> businessTypeCorporateExpr =
        new CaseBuilder()
            .when(isActive.and(qSellerInfo.businessType.eq(BusinessType.CORPORATE)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> businessTypeIndividualMakerExpr =
        new CaseBuilder()
            .when(
                isActive
                    .and(hasSellerInfo)
                    .and(businessTypeIsNull)
                    .and(qSellerInfo.verificationStatus.eq(SellerVerificationStatus.VERIFIED)))
            .then(1L)
            .otherwise(0L);
    NumberExpression<Long> businessTypeNoneExpr =
        new CaseBuilder()
            .when(isActive.and(lacksSellerInfo))
            .then(1L)
            .when(isActive.and(hasSellerInfo).and(qSellerInfo.isBusinessSeller.isFalse()))
            .then(1L)
            .when(
                isActive
                    .and(hasSellerInfo)
                    .and(businessTypeIsNull)
                    .and(qSellerInfo.verificationStatus.ne(SellerVerificationStatus.VERIFIED)))
            .then(1L)
            .otherwise(0L);

    AdminUserStatisticsAggregate result =
        queryFactory
            .select(
                Projections.constructor(
                    AdminUserStatisticsAggregate.class,
                    totalActiveExpr.sum(),
                    withdrawnExpr.sum(),
                    newUsers7Expr.sum(),
                    newUsers30Expr.sum(),
                    buyerOnlyExpr.sum(),
                    buyerAndSellerExpr.sum(),
                    marketingAgreedExpr.sum(),
                    phoneProvidedExpr.sum(),
                    sellerTermsAgreedExpr.sum(),
                    verificationVerifiedExpr.sum(),
                    verificationPendingExpr.sum(),
                    verificationInProgressExpr.sum(),
                    verificationFailedExpr.sum(),
                    verificationNoneExpr.sum(),
                    businessTypeSimplifiedExpr.sum(),
                    businessTypeNormalExpr.sum(),
                    businessTypeCorporateExpr.sum(),
                    businessTypeIndividualMakerExpr.sum(),
                    businessTypeNoneExpr.sum()))
            .from(qUser)
            .leftJoin(qSellerInfo)
            .on(qSellerInfo.user.eq(qUser))
            .fetchOne();

    if (result == null) {
      return new AdminUserStatisticsAggregate(
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
    return result;
  }

  private Page<FlatAdminUserSummaryInfoDTO> fetchAdminUserPage(
      Pageable pageable, BooleanExpression predicate) {
    QUser qUser = QUser.user;

    JPAQuery<FlatAdminUserSummaryInfoDTO> query =
        createAdminUserQuery(predicate).offset(pageable.getOffset()).limit(pageable.getPageSize());

    List<FlatAdminUserSummaryInfoDTO> content = query.fetch();
    JPAQuery<Long> countQuery = queryFactory.select(qUser.count()).from(qUser);
    if (predicate != null) {
      countQuery.where(predicate);
    }

    Long total = countQuery.fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0);
  }

  private JPAQuery<FlatAdminUserSummaryInfoDTO> createAdminUserQuery(BooleanExpression predicate) {
    QUser qUser = QUser.user;
    QIntegratedAccount qInt = QIntegratedAccount.integratedAccount;
    QSocialAccount qSoc = QSocialAccount.socialAccount;
    QUserTerms ut = QUserTerms.userTerms;
    QSellerInfo qSellerInfo = QSellerInfo.sellerInfo;
    QUserWithdrawalHistory qWithdrawalHistory = QUserWithdrawalHistory.userWithdrawalHistory;
    QUserWithdrawalHistory qWithdrawalHistorySub =
        new QUserWithdrawalHistory("userWithdrawalHistorySub");
    QUserWithdrawalHistory qWithdrawalHistoryComment =
        new QUserWithdrawalHistory("userWithdrawalHistoryComment");
    QUserWithdrawalHistory qWithdrawalHistoryCommentSub =
        new QUserWithdrawalHistory("userWithdrawalHistoryCommentSub");

    BooleanExpression marketingAgreedExists =
        JPAExpressions.selectOne()
            .from(ut)
            .where(
                ut.user.eq(qUser), ut.terms.type.eq(TermsType.MARKETING_POLICY), ut.agreed.isTrue())
            .exists();

    BooleanExpression sellerAgreedExists =
        JPAExpressions.selectOne()
            .from(ut)
            .where(
                ut.user.eq(qUser),
                ut.terms.type.eq(TermsType.SELLER_TERMS_POLICY),
                ut.agreed.isTrue())
            .exists();

    JPQLQuery<String> latestWithdrawalReason =
        JPAExpressions.select(qWithdrawalHistory.reason.stringValue())
            .from(qWithdrawalHistory)
            .where(
                qWithdrawalHistory.userId.eq(qUser.id),
                qWithdrawalHistory.withdrawalDate.eq(
                    JPAExpressions.select(qWithdrawalHistorySub.withdrawalDate.max())
                        .from(qWithdrawalHistorySub)
                        .where(qWithdrawalHistorySub.userId.eq(qUser.id))))
            .limit(1);

    JPQLQuery<String> latestWithdrawalComment =
        JPAExpressions.select(qWithdrawalHistoryComment.additionalComment)
            .from(qWithdrawalHistoryComment)
            .where(
                qWithdrawalHistoryComment.userId.eq(qUser.id),
                qWithdrawalHistoryComment.withdrawalDate.eq(
                    JPAExpressions.select(qWithdrawalHistoryCommentSub.withdrawalDate.max())
                        .from(qWithdrawalHistoryCommentSub)
                        .where(qWithdrawalHistoryCommentSub.userId.eq(qUser.id))))
            .limit(1);

    JPAQuery<FlatAdminUserSummaryInfoDTO> query =
        queryFactory
            .select(
                Projections.constructor(
                    FlatAdminUserSummaryInfoDTO.class,
                    qUser.createdAt.as("createdAt"),
                    qUser.lastLoginAt.as("lastLoginAt"),
                    sellerAgreedExists.as("isSellerTermsAgreed"),
                    qUser.userProfile.nickname.as("nickname"),
                    qInt.integratedAccountEmail.coalesce(qSoc.socialAccountEmail),
                    qUser.userProfile.phoneNumber.as("phoneNumber"),
                    qUser
                        .userStatusInfo
                        .status
                        .stringValue()
                        .coalesce(UserStatus.ACTIVE.name())
                        .as("userStatus"),
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
                    qSellerInfo.businessType.stringValue().coalesce("NONE").as("businessType"),
                    latestWithdrawalReason,
                    latestWithdrawalComment))
            .from(qUser)
            .leftJoin(qUser.integratedAccount, qInt)
            .leftJoin(qUser.socialAccount, qSoc)
            .leftJoin(qSellerInfo)
            .on(qSellerInfo.user.eq(qUser))
            .orderBy(qUser.createdAt.desc());

    if (predicate != null) {
      query.where(predicate);
    }

    return query;
  }
}
