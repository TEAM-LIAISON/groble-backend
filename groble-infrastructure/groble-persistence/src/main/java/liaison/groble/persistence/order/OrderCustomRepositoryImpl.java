package liaison.groble.persistence.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.order.dto.FlatAdminOrderSummaryInfoDTO;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.QOrder;
import liaison.groble.domain.order.repository.OrderCustomRepository;
import liaison.groble.domain.purchase.entity.QPurchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {
  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<FlatAdminOrderSummaryInfoDTO> findOrdersByPageable(Pageable pageable) {
    QOrder qOrder = QOrder.order;
    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;

    // 기본 조건 설정
    BooleanExpression conditions =
        qOrder.status.in(
            Order.OrderStatus.PAID,
            Order.OrderStatus.CANCELLED,
            Order.OrderStatus.CANCEL_REQUEST,
            Order.OrderStatus.FAILED);

    // Order를 기준으로 조회하도록 변경
    JPAQuery<FlatAdminOrderSummaryInfoDTO> query =
        jpaQueryFactory
            .select(
                Projections.constructor(
                    FlatAdminOrderSummaryInfoDTO.class,
                    qContent.id.as("contentId"),
                    qOrder.merchantUid.as("merchantUid"),
                    qOrder.createdAt.as("createdAt"),
                    qContent.contentType.stringValue().as("contentType"),
                    qContent.status.stringValue().as("contentStatus"),
                    qOrder.purchaser.name.as("purchaserName"),
                    qContent.title.as("contentTitle"),
                    qOrder.finalPrice.as("finalPrice"),
                    qOrder.status.stringValue().as("orderStatus")))
            .from(qOrder)
            .leftJoin(qPurchase)
            .on(qPurchase.order.eq(qOrder))
            .leftJoin(qContent)
            .on(qPurchase.content.eq(qContent))
            .where(conditions)
            .orderBy(qOrder.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

    List<FlatAdminOrderSummaryInfoDTO> content = query.fetch();

    // 디버깅을 위한 로그 추가
    log.debug("조회된 주문 수: {}", content.size());
    log.debug("Offset: {}, Limit: {}", pageable.getOffset(), pageable.getPageSize());

    // 동일한 조건으로 전체 카운트 조회
    Long total =
        jpaQueryFactory
            .select(qOrder.count())
            .from(qOrder)
            .leftJoin(qPurchase)
            .on(qPurchase.order.eq(qOrder))
            .leftJoin(qContent)
            .on(qPurchase.content.eq(qContent))
            .where(conditions)
            .fetchOne();

    log.debug("전체 카운트: {}", total);

    return new PageImpl<>(content, pageable, total != null ? total : 0);
  }

  @Override
  public Optional<Order> findByMerchantUidAndUserId(String merchantUid, Long userId) {
    QOrder qOrder = QOrder.order;

    Order order =
        jpaQueryFactory
            .selectFrom(qOrder)
            .where(qOrder.merchantUid.eq(merchantUid).and(qOrder.user.id.eq(userId)))
            .fetchOne();

    return Optional.ofNullable(order);
  }

  @Override
  public Optional<Order> findByMerchantUidAndGuestUserId(String merchantUid, Long guestUserId) {
    QOrder qOrder = QOrder.order;

    Order order =
        jpaQueryFactory
            .selectFrom(qOrder)
            .where(qOrder.merchantUid.eq(merchantUid).and(qOrder.guestUser.id.eq(guestUserId)))
            .fetchOne();

    return Optional.ofNullable(order);
  }
}
