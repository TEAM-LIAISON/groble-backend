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

    // 마케팅 수신 동의가 true인 레코드가 존재하는지
    BooleanExpression marketingAgreedExists =
        JPAExpressions.selectOne()
            .from(ut)
            .where(
                ut.user.eq(qUser), ut.terms.type.eq(TermsType.MARKETING_POLICY), ut.agreed.isTrue())
            .exists();

    // 판매자 이용약관 동의가 true인 레코드가 존재하는지
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
                    qUser
                        .sellerInfo
                        .businessLicenseFileUrl
                        .isNotNull()
                        .and(qUser.sellerInfo.businessLicenseFileUrl.length().gt(0))
                        .as("hasBusinessLicense"),
                    qUser
                        .sellerInfo
                        .verificationStatus
                        .stringValue()
                        .coalesce("NONE")
                        .as("verificationStatus"),
                    qUser.sellerInfo.businessSellerRequest.coalesce(false).as("isBusinessSeller"),
                    qUser
                        .sellerInfo
                        .businessType
                        .stringValue()
                        .coalesce("NONE")
                        .as("businessType")))
            .from(qUser)
            .leftJoin(qUser.integratedAccount, qInt)
            .leftJoin(qUser.socialAccount, qSoc)
            .orderBy(qUser.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    List<FlatAdminUserSummaryInfoDTO> content = query.fetch();

    // isSellerTermsAgreed 값 확인 로그 추가
    log.info("findUsersByPageable - 조회된 사용자 수: {}", content.size());
    for (int i = 0; i < content.size(); i++) {
      FlatAdminUserSummaryInfoDTO user = content.get(i);
      log.debug(
          "User[{}] - nickname: {}, isSellerTermsAgreed: {}, verificationStatus: {}",
          i,
          user.getNickname(),
          user.isSellerTermsAgreed(),
          user.getVerificationStatus());
    }

    Long total = queryFactory.select(qUser.count()).from(qUser).fetchOne();

    return new PageImpl<>(content, pageable, total != null ? total : 0);
  }
}
