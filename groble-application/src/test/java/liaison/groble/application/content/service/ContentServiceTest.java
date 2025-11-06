package liaison.groble.application.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.test.util.ReflectionTestUtils;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.dto.ContentDTO;
import liaison.groble.application.content.dto.ContentOptionDTO;
import liaison.groble.application.sell.SellerContactReader;
import liaison.groble.application.subscription.service.SubscriptionService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Category;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.content.enums.ContentStatus;
import liaison.groble.domain.content.enums.ContentType;
import liaison.groble.domain.content.repository.CategoryRepository;
import liaison.groble.domain.content.repository.ContentCustomRepository;
import liaison.groble.domain.content.repository.ContentRepository;
import liaison.groble.domain.file.repository.FileRepository;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.domain.user.entity.User;
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
}
