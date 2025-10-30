package liaison.groble.application.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.dto.ContentPayPageDTO;
import liaison.groble.application.coupon.service.CouponService;
import liaison.groble.application.payment.dto.billing.BillingKeyAction;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentMetadata;
import liaison.groble.application.payment.service.SubscriptionPaymentMetadataProvider;
import liaison.groble.domain.content.entity.CoachingOption;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.vo.UserProfile;

@ExtendWith(MockitoExtension.class)
class ContentPaymentServiceTest {

  private static final ZoneId BILLING_ZONE_ID = ZoneId.of("Asia/Seoul");

  @Mock private ContentReader contentReader;
  @Mock private CouponService couponService;
  @Mock private SubscriptionPaymentMetadataProvider subscriptionPaymentMetadataProvider;

  private ContentPaymentService contentPaymentService;

  @BeforeEach
  void setUp() {
    contentPaymentService =
        new ContentPaymentService(
            contentReader, couponService, subscriptionPaymentMetadataProvider);
  }

  @Test
  void getContentPayPage_setsNextPaymentDateForSubscriptionContent() {
    // given
    Long userId = 7L;
    Long contentId = 100L;
    Long optionId = 200L;

    Content content = createContent(ContentPaymentType.SUBSCRIPTION);
    CoachingOption option = createOption(optionId, "월 구독", BigDecimal.valueOf(30000));
    content.addOption(option);
    ReflectionTestUtils.setField(content, "id", contentId);

    when(contentReader.getContentById(contentId)).thenReturn(content);
    when(couponService.getUserCoupons(userId)).thenReturn(Collections.emptyList());
    LocalDate metadataNextPaymentDate = LocalDate.now(BILLING_ZONE_ID).plusMonths(1);
    when(subscriptionPaymentMetadataProvider.buildForContent(userId, content, option))
        .thenReturn(
            Optional.of(
                SubscriptionPaymentMetadata.builder()
                    .billingKeyAction(BillingKeyAction.REGISTER_AND_CHARGE)
                    .hasActiveBillingKey(false)
                    .billingKeyId(null)
                    .merchantUserKey(String.valueOf(userId))
                    .defaultPayMethod("CARD")
                    .payWork(BillingKeyAction.REGISTER_AND_CHARGE.getPayWork())
                    .cardVer("02")
                    .regularFlag("Y")
                    .nextPaymentDate(metadataNextPaymentDate)
                    .payYear(String.format("%04d", metadataNextPaymentDate.getYear()))
                    .payMonth(String.format("%02d", metadataNextPaymentDate.getMonthValue()))
                    .payDay(String.format("%02d", metadataNextPaymentDate.getDayOfMonth()))
                    .requiresImmediateCharge(true)
                    .build()));

    // when
    ContentPayPageDTO result = contentPaymentService.getContentPayPage(userId, contentId, optionId);

    // then
    LocalDate expectedNextPaymentDate = metadataNextPaymentDate;
    assertThat(result.getPaymentType()).isEqualTo(ContentPaymentType.SUBSCRIPTION.name());
    assertThat(result.getNextPaymentDate()).isEqualTo(expectedNextPaymentDate);
    assertThat(result.getSubscriptionMeta()).isNotNull();
    assertThat(result.getSubscriptionMeta().getPayWork())
        .isEqualTo(BillingKeyAction.REGISTER_AND_CHARGE.getPayWork());
  }

  @Test
  void getContentPayPage_returnsNullNextPaymentDateForOneTimeContent() {
    // given
    Long contentId = 101L;
    Long optionId = 201L;

    Content content = createContent(ContentPaymentType.ONE_TIME);
    CoachingOption option = createOption(optionId, "단건 결제", BigDecimal.valueOf(15000));
    content.addOption(option);
    ReflectionTestUtils.setField(content, "id", contentId);

    when(contentReader.getContentById(contentId)).thenReturn(content);
    when(subscriptionPaymentMetadataProvider.buildForContent(null, content, option))
        .thenReturn(Optional.empty());

    // when
    ContentPayPageDTO result = contentPaymentService.getContentPayPage(null, contentId, optionId);

    // then
    assertThat(result.getPaymentType()).isEqualTo(ContentPaymentType.ONE_TIME.name());
    assertThat(result.getNextPaymentDate()).isNull();
  }

  private Content createContent(ContentPaymentType paymentType) {
    User user =
        User.builder().id(1L).userProfile(UserProfile.builder().nickname("Groble").build()).build();

    Content content = new Content(user);
    content.setContentType(ContentType.COACHING);
    content.setTitle("월간 코칭");
    content.setThumbnailUrl("https://example.com/thumbnail.png");
    content.setPaymentType(paymentType);
    return content;
  }

  private CoachingOption createOption(Long optionId, String name, BigDecimal price) {
    CoachingOption option = new CoachingOption();
    option.updateCommonFields(name, "설명", price);
    ReflectionTestUtils.setField(option, "id", optionId);
    return option;
  }
}
