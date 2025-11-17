package liaison.groble.persistence.purchase;

import static com.querydsl.jpa.JPAExpressions.select;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import liaison.groble.common.response.CursorResponse;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.QContent;
import liaison.groble.domain.content.entity.QContentOption;
import liaison.groble.domain.content.entity.QContentReview;
import liaison.groble.domain.content.entity.QDocumentOption;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.dashboard.dto.FlatDashboardOverviewDTO;
import liaison.groble.domain.guest.entity.QGuestUser;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.QOrder;
import liaison.groble.domain.payment.entity.QPayplePayment;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;
import liaison.groble.domain.purchase.dto.FlatDailyTransactionStatDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentDetailDTO;
import liaison.groble.domain.purchase.dto.FlatPurchaseContentPreviewDTO;
import liaison.groble.domain.purchase.dto.FlatSellManageDetailDTO;
import liaison.groble.domain.purchase.dto.FlatTopContentStatDTO;
import liaison.groble.domain.purchase.entity.QPurchase;
import liaison.groble.domain.purchase.repository.PurchaseCustomRepository;
import liaison.groble.domain.subscription.entity.QSubscription;
import liaison.groble.domain.user.entity.QIntegratedAccount;
import liaison.groble.domain.user.entity.QSocialAccount;
import liaison.groble.domain.user.entity.QUser;
import liaison.groble.domain.user.enums.AccountType;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PurchaseCustomRepositoryImpl implements PurchaseCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Optional<FlatPurchaseContentDetailDTO> getPurchaseContentDetail(
      Long userId, String merchantUid) {
    QContent qContent = QContent.content;
    QPurchase qPurchase = QPurchase.purchase;
    QPurchase purchaseSubDetail = new QPurchase("purchaseSubDetail");
    QOrder qOrder = QOrder.order;
    QUser qUser = QUser.user;
    QDocumentOption qDocOpt = QDocumentOption.documentOption;
    QPayplePayment qPayplePayment = QPayplePayment.payplePayment;
    QSubscription qSubscription = QSubscription.subscription;

    Expression<String> documentOptionActionUrl =
        ExpressionUtils.as(
            Expressions.cases()
                .when(qContent.contentType.eq(ContentType.DOCUMENT))
                .then(
                    JPAExpressions.select(qDocOpt.documentFileUrl.coalesce(qDocOpt.documentLinkUrl))
                        .from(qDocOpt)
                        .where(qDocOpt.id.eq(qPurchase.selectedOptionId))
                        .limit(1))
                .otherwise(Expressions.nullExpression(String.class)),
            "documentOptionActionUrl");

    Expression<Integer> one = ExpressionUtils.as(Expressions.constant(1), "selectedOptionQuantity");

    // ★ isRefundable: order.status == PAID 일 때만 true
    Expression<Boolean> isRefundableExpr =
        ExpressionUtils.as(
            new CaseBuilder()
                .when(
                    qOrder
                        .status
                        .eq(Order.OrderStatus.PAID)
                        .and(qContent.contentType.eq(ContentType.COACHING)))
                .then(true)
                .otherwise(false),
            "isRefundable");

    // --- PayplePayment 서브쿼리 수정 ---

    // payType 수정
    Expression<String> payTypeExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    JPAExpressions.select(qPayplePayment.pcdPayMethod)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)
                        .in("kakaoPay", "naverPay"))
                .then(Expressions.nullExpression(String.class))
                .otherwise(
                    JPAExpressions.select(qPayplePayment.pcdPayType)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)),
            "payType");

    // payCardName 수정
    Expression<String> payCardNameExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    JPAExpressions.select(qPayplePayment.pcdPayMethod)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)
                        .eq("kakaoPay"))
                .then("카카오페이")
                .when(
                    JPAExpressions.select(qPayplePayment.pcdPayMethod)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)
                        .eq("naverPay"))
                .then("네이버페이")
                .otherwise(
                    JPAExpressions.select(qPayplePayment.pcdPayCardName)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)),
            "payCardName");

    Expression<String> payCardNumExpr =
        ExpressionUtils.as(
            JPAExpressions.select(qPayplePayment.pcdPayCardNum)
                .from(qPayplePayment)
                .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                .limit(1),
            "payCardNum");

    var subscriptionRoundExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qPurchase.user.id.isNotNull()))
                .then(
                    JPAExpressions.select(purchaseSubDetail.count().intValue())
                        .from(purchaseSubDetail)
                        .where(
                            purchaseSubDetail
                                .user
                                .id
                                .eq(qPurchase.user.id)
                                .and(purchaseSubDetail.content.id.eq(qContent.id))
                                .and(
                                    purchaseSubDetail.selectedOptionId.eq(
                                        qPurchase.selectedOptionId))
                                .and(purchaseSubDetail.purchasedAt.loe(qPurchase.purchasedAt))))
                .otherwise(Expressions.nullExpression(Integer.class)),
            "subscriptionRound");

    var isSubscriptionTerminatedExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qSubscription.gracePeriodEndsAt.isNotNull()))
                .then(true)
                .otherwise(false),
            "isSubscriptionTerminated");

    FlatPurchaseContentDetailDTO result =
        queryFactory
            .select(
                Projections.fields(
                    FlatPurchaseContentDetailDTO.class,
                    qOrder.status.stringValue().as("orderStatus"),
                    qOrder.merchantUid.as("merchantUid"),
                    qPurchase.user.id.as("userId"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    qPurchase.cancelRequestedAt.as("cancelRequestedAt"),
                    qPurchase.cancelledAt.as("cancelledAt"),
                    qContent.id.as("contentId"),
                    qContent.user.userProfile.nickname.as("sellerName"),
                    qContent.title.as("contentTitle"),
                    qPurchase.selectedOptionName.as("selectedOptionName"),
                    one,
                    qPurchase.selectedOptionType.stringValue().as("selectedOptionType"),
                    documentOptionActionUrl,
                    Expressions.cases()
                        .<Boolean>when(qPurchase.finalPrice.eq(BigDecimal.ZERO))
                        .then(true)
                        .otherwise(false)
                        .as("isFreePurchase"),
                    qPurchase.originalPrice.as("originalPrice"),
                    qPurchase.discountPrice.as("discountPrice"),
                    qPurchase.finalPrice.as("finalPrice"),
                    payTypeExpr,
                    payCardNameExpr,
                    payCardNumExpr,
                    qContent.paymentType.stringValue().as("paymentType"),
                    qSubscription.nextBillingDate.as("nextPaymentDate"),
                    subscriptionRoundExpr,
                    qSubscription.status.stringValue().as("subscriptionStatus"),
                    isSubscriptionTerminatedExpr,
                    qSubscription.lastBillingFailureReason.as("billingFailureReason"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    isRefundableExpr,
                    qPurchase.cancelReason.stringValue().as("cancelReason")))
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .leftJoin(qContent.user, qUser)
            .leftJoin(qSubscription)
            .on(qSubscription.purchase.eq(qPurchase))
            .where(qPurchase.user.id.eq(userId).and(qOrder.merchantUid.eq(merchantUid)))
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public CursorResponse<FlatPurchaseContentPreviewDTO> findMyPurchasingContentsWithCursor(
      Long userId, Long lastContentId, int size, List<Order.OrderStatus> statusList) {

    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QContentOption qContentOption = QContentOption.contentOption;
    QOrder qOrder = QOrder.order;
    QPurchase purchaseSub = new QPurchase("purchaseSubForPreview");
    QSubscription qSubscription = QSubscription.subscription;

    // 기본 조건 설정
    BooleanExpression conditions = qPurchase.user.id.eq(userId);

    // 커서 조건 추가
    if (lastContentId != null) {
      conditions = conditions.and(qContent.id.lt(lastContentId));
    }

    // 상태 필터 추가 (여러 상태 지원)
    if (statusList != null && !statusList.isEmpty()) {
      conditions = conditions.and(qOrder.status.in(statusList));
    }

    // 조회할 개수 + 1 (다음 페이지 존재 여부 확인용)
    int fetchSize = size + 1;

    // 정기결제 회차 계산
    var subscriptionRoundExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qPurchase.user.id.isNotNull()))
                .then(
                    JPAExpressions.select(purchaseSub.count().intValue())
                        .from(purchaseSub)
                        .where(
                            purchaseSub
                                .user
                                .id
                                .eq(qPurchase.user.id)
                                .and(purchaseSub.content.id.eq(qContent.id))
                                .and(purchaseSub.selectedOptionId.eq(qPurchase.selectedOptionId))
                                .and(purchaseSub.purchasedAt.loe(qPurchase.purchasedAt))))
                .otherwise(Expressions.nullExpression(Integer.class)),
            "subscriptionRound");

    // 유예기간 만료 여부
    var isSubscriptionTerminatedExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qSubscription.gracePeriodEndsAt.isNotNull()))
                .then(true)
                .otherwise(false),
            "isSubscriptionTerminated");

    // 쿼리 실행
    List<FlatPurchaseContentPreviewDTO> results =
        queryFactory
            .select(
                Projections.fields(
                    FlatPurchaseContentPreviewDTO.class,
                    qPurchase.order.merchantUid.as("merchantUid"),
                    qPurchase.user.id.as("userId"),
                    qContent.id.as("contentId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qContent.user.userProfile.nickname.as("sellerName"),
                    qPurchase.originalPrice.as("originalPrice"),
                    qPurchase.finalPrice.as("finalPrice"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qOrder.status.stringValue().as("orderStatus"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    subscriptionRoundExpr,
                    qSubscription.status.stringValue().as("subscriptionStatus"),
                    isSubscriptionTerminatedExpr,
                    qSubscription.lastBillingFailureReason.as("billingFailureReason")))
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.user, qUser)
            .leftJoin(qPurchase.order, qOrder)
            .leftJoin(qSubscription)
            .on(qSubscription.purchase.eq(qPurchase))
            .where(conditions)
            .orderBy(qContent.id.desc())
            .limit(fetchSize)
            .fetch();

    // 다음 페이지 여부 확인
    boolean hasNext = results.size() > size;

    // 실제 반환할 리스트 조정
    List<FlatPurchaseContentPreviewDTO> items = hasNext ? results.subList(0, size) : results;

    // 다음 커서 계산
    String nextCursor = null;
    if (hasNext && !items.isEmpty()) {
      nextCursor = String.valueOf(items.get(items.size() - 1).getContentId());
    }

    // 메타데이터 (여러 상태를 표시)
    String filterValue = null;
    if (statusList != null && !statusList.isEmpty()) {
      filterValue =
          statusList.stream().map(Order.OrderStatus::name).collect(Collectors.joining(","));
    }

    CursorResponse.MetaData meta =
        CursorResponse.MetaData.builder().filter(filterValue).cursorType("id").build();

    return CursorResponse.of(items, nextCursor, hasNext, 0, meta);
  }

  @Override
  public int countMyPurchasingContents(Long userId, List<Order.OrderStatus> statusList) {
    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    QOrder qOrder = QOrder.order;

    // 기본 조건 설정: 사용자 ID, 콘텐츠 타입
    BooleanExpression conditions = qPurchase.user.id.eq(userId);

    // 상태 필터 추가 (여러 상태 지원)
    if (statusList != null && !statusList.isEmpty()) {
      conditions = conditions.and(qOrder.status.in(statusList));
    }

    // 쿼리 실행: Purchase 엔티티 기준으로 카운트
    Long count =
        queryFactory
            .select(qPurchase.count())
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .where(conditions)
            .fetchOne();

    return count != null ? count.intValue() : 0;
  }

  @Override
  public Optional<FlatContentSellDetailDTO> getContentSellDetailDTO(
      Long userId, Long contentId, Long purchaseId) {
    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QGuestUser qGuestUser = QGuestUser.guestUser;
    QIntegratedAccount qIntegratedAccount = QIntegratedAccount.integratedAccount;
    QSocialAccount qSocialAccount = QSocialAccount.socialAccount;
    QOrder qOrder = QOrder.order;
    QPurchase purchaseSub = new QPurchase("purchaseSubForSellDetail");
    QSubscription qSubscription = QSubscription.subscription;

    BooleanExpression conditions =
        qContent.id.eq(contentId).and(qContent.user.id.eq(userId)).and(qPurchase.id.eq(purchaseId));

    var subscriptionRoundExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qPurchase.user.id.isNotNull()))
                .then(
                    JPAExpressions.select(purchaseSub.count().intValue())
                        .from(purchaseSub)
                        .where(
                            purchaseSub
                                .user
                                .id
                                .eq(qPurchase.user.id)
                                .and(purchaseSub.content.id.eq(qContent.id))
                                .and(purchaseSub.selectedOptionId.eq(qPurchase.selectedOptionId))
                                .and(purchaseSub.purchasedAt.loe(qPurchase.purchasedAt))))
                .otherwise(Expressions.nullExpression(Integer.class)),
            "subscriptionRound");

    var isSubscriptionTerminatedExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qSubscription.gracePeriodEndsAt.isNotNull()))
                .then(true)
                .otherwise(false),
            "isSubscriptionTerminated");

    FlatContentSellDetailDTO result =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentSellDetailDTO.class,
                    qPurchase.id.as("purchaseId"),
                    qContent.title.as("contentTitle"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    // 구매자 닉네임 - 회원/비회원 구분
                    Expressions.cases()
                        .when(qPurchase.user.isNotNull())
                        .then(qUser.userProfile.nickname)
                        .when(qPurchase.guestUser.isNotNull())
                        .then(qGuestUser.username)
                        .otherwise(Expressions.nullExpression(String.class))
                        .as("purchaserNickname"),
                    // 구매자 이메일 - 회원/비회원 구분
                    Expressions.cases()
                        .when(
                            qPurchase
                                .user
                                .isNotNull()
                                .and(qUser.accountType.eq(AccountType.INTEGRATED)))
                        .then(qIntegratedAccount.integratedAccountEmail)
                        .when(
                            qPurchase
                                .user
                                .isNotNull()
                                .and(qUser.accountType.eq(AccountType.SOCIAL)))
                        .then(qSocialAccount.socialAccountEmail)
                        .when(qPurchase.guestUser.isNotNull())
                        .then(qGuestUser.email)
                        .otherwise(Expressions.nullExpression(String.class))
                        .as("purchaserEmail"),
                    // 구매자 전화번호 - 회원/비회원 구분
                    Expressions.cases()
                        .when(qPurchase.user.isNotNull())
                        .then(qUser.userProfile.phoneNumber)
                        .when(qPurchase.guestUser.isNotNull())
                        .then(qGuestUser.phoneNumber)
                        .otherwise(Expressions.nullExpression(String.class))
                        .as("purchaserPhoneNumber"),
                    qPurchase.selectedOptionName.as("selectedOptionName"),
                    qPurchase.finalPrice.as("finalPrice"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    subscriptionRoundExpr,
                    qSubscription.status.stringValue().as("subscriptionStatus"),
                    isSubscriptionTerminatedExpr,
                    qSubscription.lastBillingFailureReason.as("billingFailureReason")))
            .from(qPurchase)
            .leftJoin(qPurchase.user, qUser)
            .leftJoin(qPurchase.guestUser, qGuestUser)
            .leftJoin(qUser.integratedAccount, qIntegratedAccount)
            .leftJoin(qUser.socialAccount, qSocialAccount)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .leftJoin(qSubscription)
            .on(qSubscription.purchase.eq(qPurchase))
            .where(conditions)
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public Page<FlatContentSellDetailDTO> getContentSellPageDTOs(
      Long userId, Long contentId, Pageable pageable) {

    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    QUser qUser = QUser.user;
    QGuestUser qGuestUser = QGuestUser.guestUser;
    QIntegratedAccount qIntegratedAcc = QIntegratedAccount.integratedAccount;
    QSocialAccount qSocialAcc = QSocialAccount.socialAccount;
    QOrder qOrder = QOrder.order;
    QPurchase purchaseSub = new QPurchase("purchaseSubForSellPage");
    QSubscription qSubscription = QSubscription.subscription;

    // 조건: contentId + 소유자
    BooleanExpression conditions =
        qPurchase
            .content
            .id
            .eq(contentId)
            .and(qPurchase.content.user.id.eq(userId))
            .and(qPurchase.purchasedAt.isNotNull());

    var subscriptionRoundExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qPurchase.user.id.isNotNull()))
                .then(
                    JPAExpressions.select(purchaseSub.count().intValue())
                        .from(purchaseSub)
                        .where(
                            purchaseSub
                                .user
                                .id
                                .eq(qPurchase.user.id)
                                .and(purchaseSub.content.id.eq(qContent.id))
                                .and(purchaseSub.selectedOptionId.eq(qPurchase.selectedOptionId))
                                .and(purchaseSub.purchasedAt.loe(qPurchase.purchasedAt))))
                .otherwise(Expressions.nullExpression(Integer.class)),
            "subscriptionRound");

    var isSubscriptionTerminatedExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qSubscription.gracePeriodEndsAt.isNotNull()))
                .then(true)
                .otherwise(false),
            "isSubscriptionTerminated");

    // 데이터 조회 쿼리 구성
    JPAQuery<FlatContentSellDetailDTO> dataQuery =
        queryFactory
            .select(
                Projections.fields(
                    FlatContentSellDetailDTO.class,
                    qPurchase.id.as("purchaseId"),
                    qContent.title.as("contentTitle"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    ExpressionUtils.as(
                        buildNicknameExpression(qPurchase, qUser, qGuestUser), "purchaserNickname"),
                    ExpressionUtils.as(
                        buildEmailExpression(
                            qPurchase, qUser, qGuestUser, qIntegratedAcc, qSocialAcc),
                        "purchaserEmail"),
                    ExpressionUtils.as(
                        buildPhoneExpression(qPurchase, qUser, qGuestUser), "purchaserPhoneNumber"),
                    qPurchase.selectedOptionName.as("selectedOptionName"),
                    qPurchase.finalPrice.as("finalPrice"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    subscriptionRoundExpr,
                    qSubscription.status.stringValue().as("subscriptionStatus"),
                    isSubscriptionTerminatedExpr,
                    qSubscription.lastBillingFailureReason.as("billingFailureReason")))
            .from(qPurchase)
            .leftJoin(qPurchase.user, qUser)
            .leftJoin(qPurchase.guestUser, qGuestUser)
            .leftJoin(qUser.integratedAccount, qIntegratedAcc)
            .leftJoin(qUser.socialAccount, qSocialAcc)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .leftJoin(qSubscription)
            .on(qSubscription.purchase.eq(qPurchase))
            .where(conditions);

    // 정렬 적용
    dataQuery = applySorting(dataQuery, pageable, qPurchase, qContent);

    // 페이징된 데이터 조회
    List<FlatContentSellDetailDTO> items =
        dataQuery.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    // 전체 개수 조회
    long total =
        Optional.ofNullable(
                queryFactory.select(qPurchase.count()).from(qPurchase).where(conditions).fetchOne())
            .orElse(0L);

    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public Page<FlatPurchaseContentPreviewDTO> findMyPurchasedContents(
      Long userId, List<Order.OrderStatus> orderStatuses, Pageable pageable) {
    QPurchase qPurchase = QPurchase.purchase;
    QOrder qOrder = QOrder.order;
    QContent qContent = QContent.content;
    QContentOption qContentOption = QContentOption.contentOption;
    QUser qUser = QUser.user;
    QPurchase purchaseSub = new QPurchase("purchaseSub");
    QPurchase purchaseSubLatest = new QPurchase("purchaseSubLatest");

    BooleanExpression conditions = qPurchase.user.id.eq(userId);
    if (orderStatuses != null) {
      conditions = conditions.and(qOrder.status.in(orderStatuses));
    }

    BooleanExpression isLatestSubscription =
        qContent
            .paymentType
            .ne(ContentPaymentType.SUBSCRIPTION)
            .or(
                JPAExpressions.selectOne()
                    .from(purchaseSubLatest)
                    .where(
                        purchaseSubLatest
                            .user
                            .id
                            .eq(qPurchase.user.id)
                            .and(purchaseSubLatest.content.id.eq(qContent.id))
                            .and(purchaseSubLatest.selectedOptionId.eq(qPurchase.selectedOptionId))
                            .and(purchaseSubLatest.purchasedAt.gt(qPurchase.purchasedAt)))
                    .notExists());

    var subscriptionRoundExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qPurchase.user.id.isNotNull()))
                .then(
                    JPAExpressions.select(purchaseSub.count().intValue())
                        .from(purchaseSub)
                        .where(
                            purchaseSub
                                .user
                                .id
                                .eq(qPurchase.user.id)
                                .and(purchaseSub.content.id.eq(qContent.id))
                                .and(purchaseSub.selectedOptionId.eq(qPurchase.selectedOptionId))
                                .and(purchaseSub.purchasedAt.loe(qPurchase.purchasedAt))))
                .otherwise(Expressions.nullExpression(Integer.class)),
            "subscriptionRound");

    JPAQuery<FlatPurchaseContentPreviewDTO> query =
        queryFactory
            .select(
                Projections.fields(
                    FlatPurchaseContentPreviewDTO.class,
                    qOrder.merchantUid.as("merchantUid"),
                    qContent.id.as("contentId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qContent.user.userProfile.nickname.as("sellerName"),
                    qPurchase.originalPrice.as("originalPrice"),
                    qPurchase.finalPrice.as("finalPrice"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qOrder.status.stringValue().as("orderStatus"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    subscriptionRoundExpr))
            .from(qPurchase)
            .leftJoin(qPurchase.user, qUser)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .where(conditions.and(isLatestSubscription));

    // 3) Pageable의 Sort 적용
    if (pageable.getSort().isUnsorted()) {
      query.orderBy(qPurchase.purchasedAt.desc()); // qContent -> qPurchase
    } else {
      for (Sort.Order order : pageable.getSort()) {
        com.querydsl.core.types.Order direction =
            order.isAscending()
                ? com.querydsl.core.types.Order.ASC
                : com.querydsl.core.types.Order.DESC;

        // purchasedAt은 Purchase 엔티티에서 처리
        if ("purchasedAt".equals(order.getProperty())) {
          query.orderBy(new OrderSpecifier<>(direction, qPurchase.purchasedAt));
        } else {
          // 다른 필드는 Content에서 처리
          PathBuilder<Content> path = new PathBuilder<>(Content.class, qContent.getMetadata());
          ComparableExpressionBase<?> expr =
              path.getComparable(order.getProperty(), Comparable.class);
          query.orderBy(new OrderSpecifier<>(direction, expr));
        }
      }
    }

    // 4) 페이징(Offset + Limit)
    List<FlatPurchaseContentPreviewDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    // 5) 전체 카운트
    long total =
        Optional.ofNullable(
                queryFactory
                    .select(qPurchase.count())
                    .from(qPurchase)
                    .leftJoin(qPurchase.content, qContent)
                    .leftJoin(qPurchase.order, qOrder) // 이 라인 추가
                    .where(conditions.and(isLatestSubscription))
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public Page<FlatPurchaseContentPreviewDTO> findMyPurchasedContentsForGuest(
      String guestPhoneNumber, List<Order.OrderStatus> orderStatuses, Pageable pageable) {
    QPurchase qPurchase = QPurchase.purchase;
    QOrder qOrder = QOrder.order;
    QContent qContent = QContent.content;
    QContentOption qContentOption = QContentOption.contentOption;
    QGuestUser qGuestUser = QGuestUser.guestUser;
    QPurchase purchaseSub = new QPurchase("guestPurchaseSub");
    QPurchase purchaseSubLatest = new QPurchase("guestPurchaseSubLatest");

    BooleanExpression conditions = qGuestUser.phoneNumber.eq(guestPhoneNumber);
    if (orderStatuses != null) {
      conditions = conditions.and(qOrder.status.in(orderStatuses));
    }

    BooleanExpression isLatestSubscription =
        qContent
            .paymentType
            .ne(ContentPaymentType.SUBSCRIPTION)
            .or(
                JPAExpressions.selectOne()
                    .from(purchaseSubLatest)
                    .where(
                        purchaseSubLatest
                            .guestUser
                            .phoneNumber
                            .eq(guestPhoneNumber)
                            .and(purchaseSubLatest.content.id.eq(qContent.id))
                            .and(purchaseSubLatest.selectedOptionId.eq(qPurchase.selectedOptionId))
                            .and(purchaseSubLatest.purchasedAt.gt(qPurchase.purchasedAt)))
                    .notExists());

    var subscriptionRoundExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    qContent
                        .paymentType
                        .eq(ContentPaymentType.SUBSCRIPTION)
                        .and(qPurchase.user.id.isNotNull()))
                .then(
                    JPAExpressions.select(purchaseSub.count().intValue())
                        .from(purchaseSub)
                        .where(
                            purchaseSub
                                .user
                                .id
                                .eq(qPurchase.user.id)
                                .and(purchaseSub.content.id.eq(qContent.id))
                                .and(purchaseSub.selectedOptionId.eq(qPurchase.selectedOptionId))
                                .and(purchaseSub.purchasedAt.loe(qPurchase.purchasedAt))))
                .otherwise(Expressions.nullExpression(Integer.class)),
            "subscriptionRound");

    JPAQuery<FlatPurchaseContentPreviewDTO> query =
        queryFactory
            .select(
                Projections.fields(
                    FlatPurchaseContentPreviewDTO.class,
                    qOrder.merchantUid.as("merchantUid"),
                    qContent.id.as("contentId"),
                    qContent.contentType.stringValue().as("contentType"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    qContent.title.as("title"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    qContent.user.userProfile.nickname.as("sellerName"),
                    qPurchase.originalPrice.as("originalPrice"),
                    qPurchase.finalPrice.as("finalPrice"),
                    ExpressionUtils.as(
                        select(qContentOption.count().intValue())
                            .from(qContentOption)
                            .where(qContentOption.content.eq(qContent)),
                        "priceOptionLength"),
                    qOrder.status.stringValue().as("orderStatus"),
                    qContent.paymentType.stringValue().as("paymentType"),
                    subscriptionRoundExpr))
            .from(qPurchase)
            .leftJoin(qPurchase.guestUser, qGuestUser)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .where(conditions.and(isLatestSubscription));

    // 3) Pageable의 Sort 적용
    if (pageable.getSort().isUnsorted()) {
      query.orderBy(qPurchase.purchasedAt.desc());
    } else {
      for (Sort.Order order : pageable.getSort()) {
        com.querydsl.core.types.Order direction =
            order.isAscending()
                ? com.querydsl.core.types.Order.ASC
                : com.querydsl.core.types.Order.DESC;

        // purchasedAt은 Purchase 엔티티에서 처리
        if ("purchasedAt".equals(order.getProperty())) {
          query.orderBy(new OrderSpecifier<>(direction, qPurchase.purchasedAt));
        } else {
          // 다른 필드는 Content에서 처리
          PathBuilder<Content> path = new PathBuilder<>(Content.class, qContent.getMetadata());
          ComparableExpressionBase<?> expr =
              path.getComparable(order.getProperty(), Comparable.class);
          query.orderBy(new OrderSpecifier<>(direction, expr));
        }
      }
    }

    // 4) 페이징(Offset + Limit)
    List<FlatPurchaseContentPreviewDTO> items =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    // 5) 전체 카운트
    long total =
        Optional.ofNullable(
                queryFactory
                    .select(qPurchase.count())
                    .from(qPurchase)
                    .leftJoin(qPurchase.guestUser, qGuestUser)
                    .leftJoin(qPurchase.content, qContent)
                    .leftJoin(qPurchase.order, qOrder)
                    .where(conditions.and(isLatestSubscription))
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(items, pageable, total);
  }

  @Override
  public Optional<FlatSellManageDetailDTO> getSellManageDetail(Long userId, Long contentId) {
    QPurchase qPurchase = QPurchase.purchase;
    QOrder qOrder = QOrder.order;
    QContent qContent = QContent.content;
    QContentReview qContentReview = QContentReview.contentReview;

    // 1번의 쿼리로 모든 통계 데이터를 가져오기
    Tuple result =
        queryFactory
            .select(
                // 총 결제 금액
                qPurchase.finalPrice.sum().coalesce(BigDecimal.ZERO),
                // 고유한 회원 구매자 수 (NULL 값 제외)
                qPurchase.user.id.countDistinct(),
                // 고유한 비회원 구매자 수 (NULL 값 제외)
                qPurchase.guestUser.id.countDistinct(),
                // 리뷰 총 개수 (서브쿼리)
                JPAExpressions.select(qContentReview.count().coalesce(0L))
                    .from(qContentReview)
                    .where(qContentReview.content.id.eq(contentId)))
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .where(
                qPurchase
                    .content
                    .id
                    .eq(contentId)
                    .and(qPurchase.content.user.id.eq(userId))
                    .and(qOrder.status.eq(Order.OrderStatus.PAID)))
            .fetchOne();

    if (result == null) {
      // 데이터가 없는 경우 기본값 반환
      return Optional.of(
          FlatSellManageDetailDTO.builder()
              .totalPaymentPrice(BigDecimal.ZERO)
              .totalPurchaseCustomer(0L)
              .totalReviewCount(0L)
              .build());
    }

    // 결과 추출 및 NULL 안전 처리
    BigDecimal totalPaymentPrice =
        Optional.ofNullable(result.get(0, BigDecimal.class)).orElse(BigDecimal.ZERO);
    Long memberCount = Optional.ofNullable(result.get(1, Long.class)).orElse(0L);
    Long guestCount = Optional.ofNullable(result.get(2, Long.class)).orElse(0L);
    Long totalReviewCount = Optional.ofNullable(result.get(3, Long.class)).orElse(0L);

    // 총 고유 구매자 수 = 회원 + 비회원
    Long totalPurchaseCustomer = memberCount + guestCount;

    return Optional.of(
        FlatSellManageDetailDTO.builder()
            .totalPaymentPrice(totalPaymentPrice)
            .totalPurchaseCustomer(totalPurchaseCustomer)
            .totalReviewCount(totalReviewCount)
            .build());
  }

  @Override
  public boolean existsByUserAndContent(Long userId, Long contentId) {

    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    BooleanExpression conditions =
        qPurchase.user.id.eq(userId).and(qPurchase.content.id.eq(contentId));
    return queryFactory
            .selectOne()
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .where(conditions)
            .fetchFirst()
        != null;
  }

  @Override
  public boolean existsByGuestUserAndContent(Long guestUserId, Long contentId) {
    QPurchase qPurchase = QPurchase.purchase;
    QContent qContent = QContent.content;
    BooleanExpression conditions =
        qPurchase.guestUser.id.eq(guestUserId).and(qPurchase.content.id.eq(contentId));
    return queryFactory
            .selectOne()
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .where(conditions)
            .fetchFirst()
        != null;
  }

  @Override
  public FlatDashboardOverviewDTO getDashboardOverviewStats(Long sellerId) {
    LocalDateTime currentMonthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

    QPurchase purchase = QPurchase.purchase;
    QContent content = QContent.content;
    QOrder order = QOrder.order;

    // 전체 통계 - 회원과 비회원 합계
    Tuple totalStats =
        queryFactory
            .select(purchase.finalPrice.sum().coalesce(BigDecimal.ZERO), purchase.count())
            .from(purchase)
            .join(purchase.content, content)
            .join(purchase.order, order)
            .where(
                content.user.id.eq(sellerId),
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                order.status.eq(Order.OrderStatus.PAID))
            .fetchOne();

    // 전체 고유 고객 수 (회원 + 비회원)
    Long totalMemberCustomers =
        queryFactory
            .select(purchase.user.id.countDistinct())
            .from(purchase)
            .join(purchase.content, content)
            .join(purchase.order, order)
            .where(
                content.user.id.eq(sellerId),
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                order.status.eq(Order.OrderStatus.PAID),
                purchase.user.isNotNull())
            .fetchOne();

    Long totalGuestCustomers =
        queryFactory
            .select(purchase.guestUser.id.countDistinct())
            .from(purchase)
            .join(purchase.content, content)
            .join(purchase.order, order)
            .where(
                content.user.id.eq(sellerId),
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                order.status.eq(Order.OrderStatus.PAID),
                purchase.guestUser.isNotNull())
            .fetchOne();

    // 이번 달 통계 - 회원과 비회원 합계
    Tuple monthStats =
        queryFactory
            .select(purchase.finalPrice.sum().coalesce(BigDecimal.ZERO), purchase.count())
            .from(purchase)
            .join(purchase.content, content)
            .join(purchase.order, order)
            .where(
                content.user.id.eq(sellerId),
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.purchasedAt.goe(currentMonthStart),
                order.status.eq(Order.OrderStatus.PAID))
            .fetchOne();

    // 이번 달 고유 고객 수 (회원 + 비회원)
    Long monthMemberCustomers =
        queryFactory
            .select(purchase.user.id.countDistinct())
            .from(purchase)
            .join(purchase.content, content)
            .join(purchase.order, order)
            .where(
                content.user.id.eq(sellerId),
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.purchasedAt.goe(currentMonthStart),
                order.status.eq(Order.OrderStatus.PAID),
                purchase.user.isNotNull())
            .fetchOne();

    Long monthGuestCustomers =
        queryFactory
            .select(purchase.guestUser.id.countDistinct())
            .from(purchase)
            .join(purchase.content, content)
            .join(purchase.order, order)
            .where(
                content.user.id.eq(sellerId),
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.purchasedAt.goe(currentMonthStart),
                order.status.eq(Order.OrderStatus.PAID),
                purchase.guestUser.isNotNull())
            .fetchOne();

    // NULL 안전 처리
    BigDecimal totalRevenue = BigDecimal.ZERO;
    Long totalSalesCount = 0L;
    Long totalCustomers;
    BigDecimal monthRevenue = BigDecimal.ZERO;
    Long monthSalesCount = 0L;
    Long recentCustomers;

    if (totalStats != null) {
      totalRevenue =
          Optional.ofNullable(totalStats.get(0, BigDecimal.class)).orElse(BigDecimal.ZERO);
      totalSalesCount = Optional.ofNullable(totalStats.get(1, Long.class)).orElse(0L);
    }

    // 전체 고유 고객 수 계산
    totalCustomers =
        (totalMemberCustomers != null ? totalMemberCustomers : 0L)
            + (totalGuestCustomers != null ? totalGuestCustomers : 0L);

    if (monthStats != null) {
      monthRevenue =
          Optional.ofNullable(monthStats.get(0, BigDecimal.class)).orElse(BigDecimal.ZERO);
      monthSalesCount = Optional.ofNullable(monthStats.get(1, Long.class)).orElse(0L);
    }

    // 이번 달 고유 고객 수 계산
    recentCustomers =
        (monthMemberCustomers != null ? monthMemberCustomers : 0L)
            + (monthGuestCustomers != null ? monthGuestCustomers : 0L);

    return FlatDashboardOverviewDTO.builder()
        .totalRevenue(totalRevenue)
        .totalSalesCount(totalSalesCount)
        .currentMonthRevenue(monthRevenue)
        .currentMonthSalesCount(monthSalesCount)
        .totalCustomers(totalCustomers)
        .recentCustomers(recentCustomers)
        .build();
  }

  @Override
  public FlatDashboardOverviewDTO getAdminDashboardOverviewStats() {
    LocalDateTime currentMonthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    QPurchase purchase = QPurchase.purchase;
    QContent content = QContent.content;

    // 전체 거래 통계 (모든 판매자 합산)
    Tuple totalStats =
        queryFactory
            .select(purchase.finalPrice.sum().coalesce(BigDecimal.ZERO), purchase.count())
            .from(purchase)
            .join(purchase.content, content)
            .where(purchase.cancelledAt.isNull(), purchase.purchasedAt.isNotNull())
            .fetchOne();

    // 이번 달 거래 통계
    Tuple monthStats =
        queryFactory
            .select(purchase.finalPrice.sum().coalesce(BigDecimal.ZERO), purchase.count())
            .from(purchase)
            .join(purchase.content, content)
            .where(
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.purchasedAt.goe(currentMonthStart))
            .fetchOne();

    // 전체 고유 고객 수 (회원 + 비회원)
    Long totalMemberCustomers =
        queryFactory
            .select(purchase.user.id.countDistinct())
            .from(purchase)
            .join(purchase.content, content)
            .where(
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.user.isNotNull())
            .fetchOne();

    Long totalGuestCustomers =
        queryFactory
            .select(purchase.guestUser.id.countDistinct())
            .from(purchase)
            .join(purchase.content, content)
            .where(
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.guestUser.isNotNull())
            .fetchOne();

    // 이번 달 고유 고객 수 (회원 + 비회원)
    Long monthMemberCustomers =
        queryFactory
            .select(purchase.user.id.countDistinct())
            .from(purchase)
            .join(purchase.content, content)
            .where(
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.purchasedAt.goe(currentMonthStart),
                purchase.user.isNotNull())
            .fetchOne();

    Long monthGuestCustomers =
        queryFactory
            .select(purchase.guestUser.id.countDistinct())
            .from(purchase)
            .join(purchase.content, content)
            .where(
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.purchasedAt.goe(currentMonthStart),
                purchase.guestUser.isNotNull())
            .fetchOne();

    // NULL 안전 처리
    BigDecimal totalRevenue = BigDecimal.ZERO;
    Long totalSalesCount = 0L;
    BigDecimal monthRevenue = BigDecimal.ZERO;
    Long monthSalesCount = 0L;

    if (totalStats != null) {
      totalRevenue =
          Optional.ofNullable(totalStats.get(0, BigDecimal.class)).orElse(BigDecimal.ZERO);
      totalSalesCount = Optional.ofNullable(totalStats.get(1, Long.class)).orElse(0L);
    }

    if (monthStats != null) {
      monthRevenue =
          Optional.ofNullable(monthStats.get(0, BigDecimal.class)).orElse(BigDecimal.ZERO);
      monthSalesCount = Optional.ofNullable(monthStats.get(1, Long.class)).orElse(0L);
    }

    // 전체 고유 고객 수 계산
    Long totalCustomers =
        (totalMemberCustomers != null ? totalMemberCustomers : 0L)
            + (totalGuestCustomers != null ? totalGuestCustomers : 0L);

    // 이번 달 고유 고객 수 계산
    Long recentCustomers =
        (monthMemberCustomers != null ? monthMemberCustomers : 0L)
            + (monthGuestCustomers != null ? monthGuestCustomers : 0L);

    return FlatDashboardOverviewDTO.builder()
        .totalRevenue(totalRevenue)
        .totalSalesCount(totalSalesCount)
        .currentMonthRevenue(monthRevenue)
        .currentMonthSalesCount(monthSalesCount)
        .totalCustomers(totalCustomers)
        .recentCustomers(recentCustomers)
        .build();
  }

  @Override
  public List<FlatDailyTransactionStatDTO> getAdminDailyTransactionStats(
      LocalDate startDate, LocalDate endDate) {
    QPurchase purchase = QPurchase.purchase;

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

    var purchaseDate = Expressions.dateTemplate(LocalDate.class, "DATE({0})", purchase.purchasedAt);

    List<Tuple> tuples =
        queryFactory
            .select(
                purchaseDate,
                purchase.finalPrice.sum().coalesce(BigDecimal.ZERO),
                purchase.id.count())
            .from(purchase)
            .where(
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.purchasedAt.goe(startDateTime),
                purchase.purchasedAt.loe(endDateTime))
            .groupBy(purchaseDate)
            .orderBy(purchaseDate.asc())
            .fetch();

    return tuples.stream()
        .map(
            tuple -> {
              LocalDate date =
                  Optional.ofNullable(tuple.get(0, Date.class)).map(Date::toLocalDate).orElse(null);

              return FlatDailyTransactionStatDTO.builder()
                  .date(date)
                  .totalRevenue(
                      Optional.ofNullable(tuple.get(1, BigDecimal.class)).orElse(BigDecimal.ZERO))
                  .totalSalesCount(Optional.ofNullable(tuple.get(2, Long.class)).orElse(0L))
                  .build();
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<FlatTopContentStatDTO> getAdminTopContentStats(
      LocalDate startDate, LocalDate endDate, long limit) {
    QPurchase purchase = QPurchase.purchase;
    QContent content = QContent.content;
    QUser seller = QUser.user;

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

    var revenueSum = purchase.finalPrice.sum();

    List<Tuple> tuples =
        queryFactory
            .select(
                content.id,
                content.title,
                seller.id,
                seller.userProfile.nickname,
                revenueSum.coalesce(BigDecimal.ZERO),
                purchase.id.count())
            .from(purchase)
            .join(purchase.content, content)
            .join(content.user, seller)
            .where(
                purchase.cancelledAt.isNull(),
                purchase.purchasedAt.isNotNull(),
                purchase.purchasedAt.goe(startDateTime),
                purchase.purchasedAt.loe(endDateTime))
            .groupBy(content.id, content.title, seller.id, seller.userProfile.nickname)
            .orderBy(revenueSum.desc())
            .limit(limit)
            .fetch();

    return tuples.stream()
        .map(
            tuple ->
                FlatTopContentStatDTO.builder()
                    .contentId(tuple.get(0, Long.class))
                    .contentTitle(tuple.get(1, String.class))
                    .sellerId(tuple.get(2, Long.class))
                    .sellerNickname(tuple.get(3, String.class))
                    .totalRevenue(
                        Optional.ofNullable(tuple.get(4, BigDecimal.class)).orElse(BigDecimal.ZERO))
                    .totalSalesCount(Optional.ofNullable(tuple.get(5, Long.class)).orElse(0L))
                    .build())
        .collect(Collectors.toList());
  }

  /** 구매자 닉네임 표현식을 생성합니다. 회원인 경우 User의 닉네임을, 비회원인 경우 GuestUser의 username을 반환합니다. */
  private Expression<String> buildNicknameExpression(
      QPurchase qPurchase, QUser qUser, QGuestUser qGuestUser) {
    return Expressions.cases()
        .when(qPurchase.user.isNotNull())
        .then(qUser.userProfile.nickname)
        .otherwise(qGuestUser.username);
  }

  /** 구매자 이메일 표현식을 생성합니다. 회원인 경우 계정 타입에 따라 이메일을, 비회원인 경우 GuestUser의 이메일을 반환합니다. */
  private Expression<String> buildEmailExpression(
      QPurchase qPurchase,
      QUser qUser,
      QGuestUser qGuestUser,
      QIntegratedAccount qIntegratedAcc,
      QSocialAccount qSocialAcc) {
    return Expressions.cases()
        .when(qPurchase.user.isNotNull().and(qUser.accountType.eq(AccountType.INTEGRATED)))
        .then(qIntegratedAcc.integratedAccountEmail)
        .when(qPurchase.user.isNotNull().and(qUser.accountType.eq(AccountType.SOCIAL)))
        .then(qSocialAcc.socialAccountEmail)
        .otherwise(qGuestUser.email);
  }

  /** 구매자 전화번호 표현식을 생성합니다. 회원인 경우 User의 전화번호를, 비회원인 경우 GuestUser의 전화번호를 반환합니다. */
  private Expression<String> buildPhoneExpression(
      QPurchase qPurchase, QUser qUser, QGuestUser qGuestUser) {
    return Expressions.cases()
        .when(qPurchase.user.isNotNull())
        .then(qUser.userProfile.phoneNumber)
        .otherwise(qGuestUser.phoneNumber);
  }

  /** 정렬 조건을 적용합니다. */
  private JPAQuery<FlatContentSellDetailDTO> applySorting(
      JPAQuery<FlatContentSellDetailDTO> query,
      Pageable pageable,
      QPurchase qPurchase,
      QContent qContent) {
    if (pageable.getSort().isUnsorted()) {
      return query.orderBy(qPurchase.purchasedAt.desc());
    }

    for (Sort.Order sortOrder : pageable.getSort()) {
      com.querydsl.core.types.Order direction =
          sortOrder.isAscending()
              ? com.querydsl.core.types.Order.ASC
              : com.querydsl.core.types.Order.DESC;

      String property = sortOrder.getProperty();
      if ("purchasedAt".equals(property)) {
        query = query.orderBy(new OrderSpecifier<>(direction, qPurchase.purchasedAt));
      } else {
        PathBuilder<Content> contentPath = new PathBuilder<>(Content.class, qContent.getMetadata());
        ComparableExpressionBase<?> expr = contentPath.getComparable(property, Comparable.class);
        query = query.orderBy(new OrderSpecifier<>(direction, expr));
      }
    }

    return query;
  }

  @Override
  public Optional<FlatPurchaseContentDetailDTO> getPurchaseContentDetailForGuest(
      String merchantUid) {
    QContent qContent = QContent.content;
    QPurchase qPurchase = QPurchase.purchase;
    QOrder qOrder = QOrder.order;
    QGuestUser qGuestUser = QGuestUser.guestUser;
    QDocumentOption qDocOpt = QDocumentOption.documentOption;
    QPayplePayment qPayplePayment = QPayplePayment.payplePayment;
    QSubscription qSubscription = QSubscription.subscription;

    Expression<String> documentOptionActionUrl =
        ExpressionUtils.as(
            Expressions.cases()
                .when(qContent.contentType.eq(ContentType.DOCUMENT))
                .then(
                    JPAExpressions.select(qDocOpt.documentFileUrl.coalesce(qDocOpt.documentLinkUrl))
                        .from(qDocOpt)
                        .where(qDocOpt.id.eq(qPurchase.selectedOptionId))
                        .limit(1))
                .otherwise(Expressions.nullExpression(String.class)),
            "documentOptionActionUrl");

    Expression<Integer> one = ExpressionUtils.as(Expressions.constant(1), "selectedOptionQuantity");

    // ★ isRefundable: order.status == PAID 일 때만 true
    Expression<Boolean> isRefundableExpr =
        ExpressionUtils.as(
            new CaseBuilder()
                .when(
                    qOrder
                        .status
                        .eq(Order.OrderStatus.PAID)
                        .and(qContent.contentType.eq(ContentType.COACHING)))
                .then(true)
                .otherwise(false),
            "isRefundable");

    // --- PayplePayment 서브쿼리 수정 ---

    // payType 수정
    Expression<String> payTypeExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    JPAExpressions.select(qPayplePayment.pcdPayMethod)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)
                        .in("kakaoPay", "naverPay"))
                .then(Expressions.nullExpression(String.class))
                .otherwise(
                    JPAExpressions.select(qPayplePayment.pcdPayType)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)),
            "payType");

    // payCardName 수정
    Expression<String> payCardNameExpr =
        ExpressionUtils.as(
            Expressions.cases()
                .when(
                    JPAExpressions.select(qPayplePayment.pcdPayMethod)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)
                        .eq("kakaoPay"))
                .then("카카오페이")
                .when(
                    JPAExpressions.select(qPayplePayment.pcdPayMethod)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)
                        .eq("naverPay"))
                .then("네이버페이")
                .otherwise(
                    JPAExpressions.select(qPayplePayment.pcdPayCardName)
                        .from(qPayplePayment)
                        .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                        .limit(1)),
            "payCardName");

    Expression<String> payCardNumExpr =
        ExpressionUtils.as(
            JPAExpressions.select(qPayplePayment.pcdPayCardNum)
                .from(qPayplePayment)
                .where(qPayplePayment.pcdPayOid.eq(qOrder.merchantUid))
                .limit(1),
            "payCardNum");

    FlatPurchaseContentDetailDTO result =
        queryFactory
            .select(
                Projections.fields(
                    FlatPurchaseContentDetailDTO.class,
                    qOrder.status.stringValue().as("orderStatus"),
                    qOrder.merchantUid.as("merchantUid"),
                    qPurchase.user.id.as("userId"),
                    qPurchase.purchasedAt.as("purchasedAt"),
                    qPurchase.cancelRequestedAt.as("cancelRequestedAt"),
                    qPurchase.cancelledAt.as("cancelledAt"),
                    qContent.id.as("contentId"),
                    qContent.user.userProfile.nickname.as("sellerName"),
                    qContent.title.as("contentTitle"),
                    qPurchase.selectedOptionName.as("selectedOptionName"),
                    one,
                    qPurchase.selectedOptionType.stringValue().as("selectedOptionType"),
                    documentOptionActionUrl,
                    Expressions.cases()
                        .<Boolean>when(qPurchase.finalPrice.eq(BigDecimal.ZERO))
                        .then(true)
                        .otherwise(false)
                        .as("isFreePurchase"),
                    qPurchase.originalPrice.as("originalPrice"),
                    qPurchase.discountPrice.as("discountPrice"),
                    qPurchase.finalPrice.as("finalPrice"),
                    payTypeExpr,
                    payCardNameExpr,
                    payCardNumExpr,
                    qContent.paymentType.stringValue().as("paymentType"),
                    qSubscription.nextBillingDate.as("nextPaymentDate"),
                    qContent.thumbnailUrl.as("thumbnailUrl"),
                    isRefundableExpr,
                    qPurchase.cancelReason.stringValue().as("cancelReason")))
            .from(qPurchase)
            .leftJoin(qPurchase.content, qContent)
            .leftJoin(qPurchase.order, qOrder)
            .leftJoin(qPurchase.guestUser, qGuestUser)
            .leftJoin(qSubscription)
            .on(qSubscription.purchase.eq(qPurchase))
            .where(qOrder.merchantUid.eq(merchantUid))
            .fetchOne();

    return Optional.ofNullable(result);
  }
}
