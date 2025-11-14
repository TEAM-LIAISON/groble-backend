package liaison.groble.application.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.dto.ContentDTO;
import liaison.groble.application.content.dto.ContentOptionDTO;
import liaison.groble.application.sell.SellerContactReader;
import liaison.groble.application.subscription.service.SubscriptionService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.entity.Category;
import liaison.groble.domain.content.entity.CoachingOption;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.AdminContentCheckingStatus;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.content.enums.SubscriptionSellStatus;
import liaison.groble.domain.content.repository.CategoryRepository;
import liaison.groble.domain.content.repository.ContentCustomRepository;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.file.repository.FileRepository;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.vo.UserProfile;
import liaison.groble.external.discord.service.content.DiscordContentRegisterReportService;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

  @Mock private UserReader userReader;
  @Mock private ContentReader contentReader;
  @Mock private ContentReviewReader contentReviewReader;
  @Mock private SellerContactReader sellerContactReader;
  @Mock private ContentRepository contentRepository;
  @Mock private ContentCustomRepository contentCustomRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private FileRepository fileRepository;
  @Mock private PurchaseRepository purchaseRepository;
  @Mock private DiscordContentRegisterReportService discordContentRegisterReportService;
  @Mock private SubscriptionService subscriptionService;

  private ContentService contentService;

  @BeforeEach
  void setUp() {
    contentService =
        new ContentService(
            userReader,
            contentReader,
            contentReviewReader,
            sellerContactReader,
            contentRepository,
            contentCustomRepository,
            categoryRepository,
            fileRepository,
            purchaseRepository,
            discordContentRegisterReportService,
            subscriptionService);
  }

  @Test
  void draftContent_updatesPaymentTypeWhenRequestContainsSubscription() {
    // given
    Long userId = 1L;
    Long contentId = 10L;

    User user = User.builder().id(userId).build();
    Content content = new Content(user);
    ReflectionTestUtils.setField(content, "id", contentId);
    content.setPaymentType(ContentPaymentType.ONE_TIME);
    content.setStatus(ContentStatus.DRAFT);

    when(userReader.getUserById(userId)).thenReturn(user);
    when(contentReader.getContentWithSeller(contentId)).thenReturn(content);
    when(contentRepository.save(any(Content.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ContentDTO requestDto =
        ContentDTO.builder()
            .contentId(contentId)
            .paymentType(ContentPaymentType.SUBSCRIPTION.name())
            .options(Collections.emptyList())
            .build();

    // when
    contentService.draftContent(userId, requestDto);

    // then
    ArgumentCaptor<Content> captor = ArgumentCaptor.forClass(Content.class);
    verify(contentRepository).save(captor.capture());
    assertThat(captor.getValue().getPaymentType()).isEqualTo(ContentPaymentType.SUBSCRIPTION);
  }

  @Test
  void draftContent_clearsTitleWhenNotProvided() {
    // given
    Long userId = 2L;
    Long contentId = 30L;

    User user = User.builder().id(userId).build();
    Content content = new Content(user);
    ReflectionTestUtils.setField(content, "id", contentId);
    content.setTitle("Original Title");
    content.setStatus(ContentStatus.DRAFT);

    when(userReader.getUserById(userId)).thenReturn(user);
    when(contentReader.getContentWithSeller(contentId)).thenReturn(content);
    when(contentRepository.save(any(Content.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ContentDTO requestDto =
        ContentDTO.builder()
            .contentId(contentId)
            .paymentType(ContentPaymentType.ONE_TIME.name())
            .isSearchExposed(false)
            .contentType(ContentType.COACHING.name())
            .serviceTarget("1")
            .build();

    // when
    ContentDTO response = contentService.draftContent(userId, requestDto);

    // then
    ArgumentCaptor<Content> captor = ArgumentCaptor.forClass(Content.class);
    verify(contentRepository).save(captor.capture());
    assertThat(captor.getValue().getTitle()).isNull();
    assertThat(response.getTitle()).isNull();
  }

  @Test
  void registerContent_updatesPaymentType() {
    // given
    Long userId = 1L;
    Long contentId = 20L;
    String categoryCode = "TEST_CATEGORY";

    User user = User.builder().id(userId).build();
    Content content = new Content(user);
    ReflectionTestUtils.setField(content, "id", contentId);
    content.setPaymentType(ContentPaymentType.ONE_TIME);
    content.setContentType(ContentType.COACHING);
    content.setStatus(ContentStatus.DRAFT);

    Category category = new Category();
    ReflectionTestUtils.setField(category, "code", categoryCode);
    ReflectionTestUtils.setField(category, "id", 5L);

    when(userReader.getUserById(userId)).thenReturn(user);
    when(contentReader.getContentWithSeller(contentId)).thenReturn(content);
    when(categoryRepository.findByCode(categoryCode)).thenReturn(Optional.of(category));
    when(contentRepository.save(any(Content.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ContentDTO requestDto =
        ContentDTO.builder()
            .contentId(contentId)
            .title("테스트 콘텐츠")
            .contentType(ContentType.COACHING.name())
            .categoryId(categoryCode)
            .paymentType(ContentPaymentType.SUBSCRIPTION.name())
            .contentIntroduction("소개")
            .options(
                List.of(
                    ContentOptionDTO.builder()
                        .name("옵션A")
                        .description("설명")
                        .price(BigDecimal.TEN)
                        .build()))
            .build();

    // when
    contentService.registerContent(userId, requestDto);

    // then
    ArgumentCaptor<Content> captor = ArgumentCaptor.forClass(Content.class);
    verify(contentRepository).save(captor.capture());
    assertThat(captor.getValue().getPaymentType()).isEqualTo(ContentPaymentType.SUBSCRIPTION);
  }

  @Test
  void draftContent_subscriptionWithSoldOptionsAddsNewOnes() {
    // given
    Long userId = 3L;
    Long contentId = 40L;
    Long soldOptionId = 100L;

    User user = User.builder().id(userId).build();
    Content content = new Content(user);
    ReflectionTestUtils.setField(content, "id", contentId);
    content.setStatus(ContentStatus.DRAFT);
    content.setContentType(ContentType.COACHING);
    content.setPaymentType(ContentPaymentType.SUBSCRIPTION);
    content.incrementSaleCount();

    CoachingOption soldOption = new CoachingOption();
    soldOption.updateCommonFields("기존옵션", "판매 이력 있음", BigDecimal.ONE);
    content.addOption(soldOption);
    ReflectionTestUtils.setField(soldOption, "id", soldOptionId);

    CoachingOption unsoldOption = new CoachingOption();
    unsoldOption.updateCommonFields("임시옵션", "판매 이력 없음", BigDecimal.valueOf(5));
    content.addOption(unsoldOption);
    ReflectionTestUtils.setField(unsoldOption, "id", soldOptionId + 1);

    when(userReader.getUserById(userId)).thenReturn(user);
    when(contentReader.getContentWithSeller(contentId)).thenReturn(content);
    when(contentRepository.save(any(Content.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(purchaseRepository.findSoldOptionIdsByContentId(contentId))
        .thenReturn(List.of(soldOptionId));

    ContentDTO requestDto =
        ContentDTO.builder()
            .contentId(contentId)
            .paymentType(ContentPaymentType.SUBSCRIPTION.name())
            .contentType(ContentType.COACHING.name())
            .options(
                List.of(
                    ContentOptionDTO.builder()
                        .contentOptionId(soldOptionId)
                        .name("기존옵션")
                        .description("판매 이력 있음")
                        .price(BigDecimal.ONE)
                        .build(),
                    ContentOptionDTO.builder()
                        .name("새 캔디 옵션")
                        .description("판매 이력 없음")
                        .price(BigDecimal.valueOf(20))
                        .build()))
            .build();

    // when
    ContentDTO result = contentService.draftContent(userId, requestDto);

    // then
    assertThat(soldOption.isActive()).isTrue();
    assertThat(unsoldOption.isActive()).isFalse();
    assertThat(
            content.getOptions().stream()
                .filter(option -> option.isActive() && "새 캔디 옵션".equals(option.getName()))
                .count())
        .isEqualTo(1);
    assertThat(result.getOptions())
        .extracting(ContentOptionDTO::getName)
        .contains("기존옵션", "새 캔디 옵션");
  }

  @Test
  void stopContent_transitionsActiveContentToDiscontinued() {
    // given
    Long userId = 11L;
    Long contentId = 42L;
    User user = User.builder().id(userId).build();
    Content content = new Content(user);
    ReflectionTestUtils.setField(content, "id", contentId);
    content.setStatus(ContentStatus.ACTIVE);
    content.setAdminContentCheckingStatus(AdminContentCheckingStatus.VALIDATED);

    when(contentReader.getContentByStatusAndId(contentId, ContentStatus.ACTIVE))
        .thenReturn(content);
    when(contentRepository.save(any(Content.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(purchaseRepository.findSoldOptionIdsByContentId(contentId))
        .thenReturn(Collections.emptyList());

    // when
    ContentDTO result = contentService.stopContent(userId, contentId);

    // then
    assertThat(content.getStatus()).isEqualTo(ContentStatus.DISCONTINUED);
    assertThat(content.getAdminContentCheckingStatus())
        .isEqualTo(AdminContentCheckingStatus.PENDING);
    assertThat(result.getStatus()).isEqualTo(ContentStatus.DISCONTINUED.name());
  }

  @Test
  void convertToSale_allowsPausedContent() {
    // given
    Long userId = 7L;
    Long contentId = 88L;
    User user =
        User.builder()
            .id(userId)
            .userProfile(UserProfile.builder().nickname("테스터").build())
            .build();
    Content content = new Content(user);
    ReflectionTestUtils.setField(content, "id", contentId);
    content.setStatus(ContentStatus.PAUSED);
    content.setContentType(ContentType.COACHING);
    content.setTitle("Paused Content");

    when(contentReader.getContentWithSeller(contentId)).thenReturn(content);
    when(contentReader.isAvailableForSale(contentId)).thenReturn(true);

    // when
    contentService.convertToSale(userId, contentId);

    // then
    assertThat(content.getStatus()).isEqualTo(ContentStatus.ACTIVE);
    verify(discordContentRegisterReportService).sendCreateContentRegisterReport(any());
  }

  @Test
  void getMySellingContents_activeStateIncludesPaused() {
    Long userId = 99L;
    User user = User.builder().id(userId).build();
    Pageable pageable = PageRequest.of(0, 12, Sort.by("createdAt").descending());
    when(userReader.getUserById(userId)).thenReturn(user);
    when(sellerContactReader.getContactsByUser(user)).thenReturn(Collections.emptyList());
    Page<FlatContentPreviewDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
    when(contentReader.findMyContentsWithStatus(
            any(Pageable.class),
            eq(userId),
            anyList(),
            any(),
            any(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean()))
        .thenReturn(emptyPage);

    contentService.getMySellingContents(userId, pageable, "ACTIVE");

    ArgumentCaptor<List<ContentStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<Boolean> excludeTerminatedCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<Boolean> excludePausedCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<Boolean> includePausedCaptor = ArgumentCaptor.forClass(Boolean.class);
    verify(contentReader)
        .findMyContentsWithStatus(
            any(Pageable.class),
            eq(userId),
            statusesCaptor.capture(),
            isNull(),
            isNull(),
            excludeTerminatedCaptor.capture(),
            excludePausedCaptor.capture(),
            includePausedCaptor.capture());

    assertThat(statusesCaptor.getValue())
        .containsExactlyInAnyOrder(ContentStatus.ACTIVE, ContentStatus.PAUSED);
    assertThat(excludeTerminatedCaptor.getValue()).isTrue();
    assertThat(excludePausedCaptor.getValue()).isTrue();
    assertThat(includePausedCaptor.getValue()).isFalse();
  }

  @Test
  void getMySellingContents_discontinuedFiltersSubscriptionTermination() {
    Long userId = 77L;
    User user = User.builder().id(userId).build();
    Pageable pageable = PageRequest.of(0, 12, Sort.by("createdAt"));
    when(userReader.getUserById(userId)).thenReturn(user);
    when(sellerContactReader.getContactsByUser(user)).thenReturn(Collections.emptyList());
    Page<FlatContentPreviewDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
    when(contentReader.findMyContentsWithStatus(
            any(Pageable.class),
            eq(userId),
            anyList(),
            any(),
            any(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean()))
        .thenReturn(emptyPage);

    contentService.getMySellingContents(userId, pageable, "DISCONTINUED");

    ArgumentCaptor<List<ContentStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<ContentPaymentType> paymentCaptor =
        ArgumentCaptor.forClass(ContentPaymentType.class);
    ArgumentCaptor<SubscriptionSellStatus> subscriptionCaptor =
        ArgumentCaptor.forClass(SubscriptionSellStatus.class);
    ArgumentCaptor<Boolean> excludeTerminatedCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<Boolean> excludePausedCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<Boolean> includePausedCaptor = ArgumentCaptor.forClass(Boolean.class);

    verify(contentReader)
        .findMyContentsWithStatus(
            any(Pageable.class),
            eq(userId),
            statusesCaptor.capture(),
            paymentCaptor.capture(),
            subscriptionCaptor.capture(),
            excludeTerminatedCaptor.capture(),
            excludePausedCaptor.capture(),
            includePausedCaptor.capture());

    assertThat(statusesCaptor.getValue())
        .containsExactlyInAnyOrder(ContentStatus.ACTIVE, ContentStatus.PAUSED);
    assertThat(paymentCaptor.getValue()).isEqualTo(ContentPaymentType.SUBSCRIPTION);
    assertThat(subscriptionCaptor.getValue()).isEqualTo(SubscriptionSellStatus.TERMINATED);
    assertThat(excludeTerminatedCaptor.getValue()).isFalse();
    assertThat(excludePausedCaptor.getValue()).isFalse();
    assertThat(includePausedCaptor.getValue()).isFalse();
  }

  @Test
  void getMySellingContents_draftStateIncludesSubscriptionPaused() {
    Long userId = 55L;
    User user = User.builder().id(userId).build();
    Pageable pageable = PageRequest.of(0, 12, Sort.by("createdAt"));
    when(userReader.getUserById(userId)).thenReturn(user);
    when(sellerContactReader.getContactsByUser(user)).thenReturn(Collections.emptyList());
    Page<FlatContentPreviewDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
    when(contentReader.findMyContentsWithStatus(
            any(Pageable.class),
            eq(userId),
            anyList(),
            any(),
            any(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean()))
        .thenReturn(emptyPage);

    contentService.getMySellingContents(userId, pageable, "DRAFT");

    ArgumentCaptor<List<ContentStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<Boolean> includePausedCaptor = ArgumentCaptor.forClass(Boolean.class);
    verify(contentReader)
        .findMyContentsWithStatus(
            any(Pageable.class),
            eq(userId),
            statusesCaptor.capture(),
            isNull(),
            isNull(),
            anyBoolean(),
            anyBoolean(),
            includePausedCaptor.capture());

    assertThat(statusesCaptor.getValue())
        .containsExactlyInAnyOrder(ContentStatus.DRAFT, ContentStatus.DISCONTINUED);
    assertThat(includePausedCaptor.getValue()).isTrue();
  }
}
