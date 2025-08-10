package liaison.groble.persistence.coupon;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.coupon.dto.FlatUserCouponCardDTO;
import liaison.groble.domain.coupon.entity.QCouponTemplate;
import liaison.groble.domain.coupon.entity.QUserCoupon;
import liaison.groble.domain.coupon.enums.CouponStatus;
import liaison.groble.domain.coupon.repository.UserCouponCustomRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserCouponCustomRepositoryImpl implements UserCouponCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<FlatUserCouponCardDTO> findAllUsableCouponsByUserId(Long userId) {
    if (userId == null) {
      return Collections.emptyList(); // 또는 예외 처리
    }

    QUserCoupon qUserCoupon = QUserCoupon.userCoupon;
    QCouponTemplate qCouponTemplate = QCouponTemplate.couponTemplate;

    return jpaQueryFactory
        .select(
            Projections.fields(
                FlatUserCouponCardDTO.class,
                qUserCoupon.couponCode.as("couponCode"),
                qCouponTemplate.name.as("name"),
                qCouponTemplate.couponType.stringValue().as("couponType"),
                qCouponTemplate.discountValue.as("discountValue"),
                qCouponTemplate.validUntil.as("validUntil"),
                qCouponTemplate.minOrderPrice.as("minOrderPrice")))
        .from(qUserCoupon)
        .leftJoin(qUserCoupon.couponTemplate, qCouponTemplate)
        .where(
            qUserCoupon.user.id.eq(userId),
            qUserCoupon.status.eq(CouponStatus.ISSUED),
            qCouponTemplate.validFrom.loe(LocalDateTime.now()),
            qCouponTemplate.validUntil.goe(LocalDateTime.now()))
        .fetch();
  }
}
