package liaison.groble.persistence.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.terms.entity.QUserTerms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.dto.FlatAdminUserSummaryInfoDTO;
import liaison.groble.domain.user.entity.QIntegratedAccount;
import liaison.groble.domain.user.entity.QSellerInfo;
import liaison.groble.domain.user.entity.QSocialAccount;
import liaison.groble.domain.user.entity.QUser;
import liaison.groble.domain.user.entity.QUserWithdrawalHistory;
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
