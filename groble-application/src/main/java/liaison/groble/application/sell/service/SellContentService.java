package liaison.groble.application.sell.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.ContentReplyReader;
import liaison.groble.application.content.ContentReplyWriter;
import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.content.dto.review.ReviewReplyDTO;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;
import liaison.groble.application.sell.dto.SellManageDetailDTO;
import liaison.groble.application.sell.dto.SellManagePageDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;
import liaison.groble.domain.content.dto.FlatReviewReplyDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentReply;
import liaison.groble.domain.content.entity.ContentReview;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;
import liaison.groble.domain.purchase.dto.FlatSellManageDetailDTO;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.user.entity.User;
import liaison.groble.external.discord.dto.DeleteReviewRequestReportDTO;
import liaison.groble.external.discord.service.content.DiscordDeleteReviewRequestReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellContentService {

  // Reader
  private final ContentReviewReader contentReviewReader;
  private final PurchaseReader purchaseReader;
  private final OrderReader orderReader;
  private final ContentReader contentReader;
  private final ContentReplyReader contentReplyReader;
  private final UserReader userReader;

  // Writer
  private final ContentReviewWriter contentReviewWriter;
  private final ContentReplyWriter contentReplyWriter;

  private final DiscordDeleteReviewRequestReportService discordDeleteReviewRequestReportService;
  private final NotificationService notificationService;

  @Transactional(readOnly = true)
  public SellManagePageDTO getSellManagePage(Long userId, Long contentId) {
    Content content = contentReader.getContentById(contentId);
    FlatSellManageDetailDTO flatSellManageDetailDTO =
        purchaseReader.getSellManageDetail(userId, contentId);
    // 상위 5개 판매 내역 조회
    PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "purchasedAt"));
    Page<FlatContentSellDetailDTO> sellPage =
        purchaseReader.getContentSells(userId, contentId, pageRequest);
    List<ContentSellDetailDTO> contentSellList =
        sellPage.getContent().stream().map(this::convertFlatDTOToDetailDTO).toList();

    // 상위 5개 리뷰 조회
    PageRequest reviewPageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<FlatContentReviewDetailDTO> reviewPage =
        contentReviewReader.getContentReviews(userId, contentId, reviewPageRequest);
    List<ContentReviewDetailDTO> contentReviewList =
        reviewPage.getContent().stream().map(this::convertFlatDTOToDetailDTO).toList();

    return SellManagePageDTO.builder()
        .title(content.getTitle())
        .sellManageDetail(buildSellManageDetailDTO(flatSellManageDetailDTO))
        .contentSellDetailList(contentSellList)
        .contentReviewDetailList(contentReviewList)
        .build();
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentSellDetailDTO> getContentSells(
      Long userId, Long contentId, Pageable pageable) {
    Page<FlatContentSellDetailDTO> page =
        purchaseReader.getContentSells(userId, contentId, pageable);

    List<ContentSellDetailDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToDetailDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentReviewDetailDTO> getContentReviews(
      Long userId, Long contentId, Pageable pageable) {

    Page<FlatContentReviewDetailDTO> page =
        contentReviewReader.getContentReviews(userId, contentId, pageable);

    List<ContentReviewDetailDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToDetailDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public ContentReviewDetailDTO getContentReviewDetail(Long userId, Long contentId, Long reviewId) {
    FlatContentReviewDetailDTO contentReviewDetailDTO =
        contentReviewReader.getContentReviewDetail(userId, contentId, reviewId);

    List<FlatReviewReplyDTO> flatReviewReplyDTOs =
        contentReplyReader.findRepliesByReviewId(reviewId);

    List<ReviewReplyDTO> reviewReplyDTOs =
        flatReviewReplyDTOs.stream()
            .map(
                reply ->
                    ReviewReplyDTO.builder()
                        .replyId(reply.getReplyId())
                        .replyContent(reply.getReplyContent())
                        .replierNickname(reply.getReplierNickname())
                        .createdAt(reply.getCreatedAt())
                        .build())
            .toList();

    return buildContentReviewDetail(contentReviewDetailDTO, reviewReplyDTOs);
  }

  @Transactional(readOnly = true)
  public ContentReviewDetailDTO getContentReviewDetail(Long userId, String merchantUid) {

    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    Long contentId = purchase.getContent().getId();

    Optional<FlatContentReviewDetailDTO> reviewOpt =
        contentReviewReader.getContentReviewDetail(userId, contentId);

    // 2) 리뷰 + 리플리 조회 후 DTO 조합
    return reviewOpt
        .map(
            flat -> {
              // flat.getReviewId() 기준으로 답글들 불러오기
              List<FlatReviewReplyDTO> replies =
                  contentReplyReader.findRepliesByReviewId(flat.getReviewId());
              // (2) Collectors.toList() 사용
              List<ReviewReplyDTO> replyDTOs =
                  replies.stream()
                      .map(
                          reply ->
                              ReviewReplyDTO.builder()
                                  .replyId(reply.getReplyId())
                                  .replyContent(reply.getReplyContent())
                                  .replierNickname(reply.getReplierNickname())
                                  .createdAt(reply.getCreatedAt())
                                  .build())
                      .collect(Collectors.toList());
              return buildContentReviewDetail(flat, replyDTOs);
            })
        .orElse(null); // 리뷰가 없으면 null 반환
  }

  // 비회원 콘텐츠 리뷰 상세 조회
  @Transactional(readOnly = true)
  public ContentReviewDetailDTO getContentReviewDetailForGuest(
      Long guestUserId, String merchantUid) {
    Order order = orderReader.getOrderByMerchantUidAndGuestUserId(merchantUid, guestUserId);
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());
    Long contentId = purchase.getContent().getId();
    Optional<FlatContentReviewDetailDTO> reviewOpt =
        contentReviewReader.getContentReviewDetailForGuest(guestUserId, contentId);
    // 2) 리뷰 + 리플리 조회 후 DTO 조합
    return reviewOpt
        .map(
            flat -> {
              // flat.getReviewId() 기준으로 답글들 불러오기
              List<FlatReviewReplyDTO> replies =
                  contentReplyReader.findRepliesByReviewId(flat.getReviewId());
              // (2) Collectors.toList() 사용
              List<ReviewReplyDTO> replyDTOs =
                  replies.stream()
                      .map(
                          reply ->
                              ReviewReplyDTO.builder()
                                  .replyId(reply.getReplyId())
                                  .replyContent(reply.getReplyContent())
                                  .replierNickname(reply.getReplierNickname())
                                  .createdAt(reply.getCreatedAt())
                                  .build())
                      .collect(Collectors.toList());
              return buildContentReviewDetail(flat, replyDTOs);
            })
        .orElse(null); // 리뷰가 없으면 null 반환
  }

  @Transactional
  public void deleteReviewRequest(Long userId, Long reviewId) {
    contentReviewWriter.updateContentReviewStatusToDeleteRequested(userId, reviewId);

    // 디스코드 알림 발송
    DeleteReviewRequestReportDTO dto = buildDeleteReviewRequestReportDTO(userId, reviewId);
    discordDeleteReviewRequestReportService.sendDeleteReviewRequestReport(dto);
  }

  @Transactional
  public ReplyContentDTO addReviewReply(
      Long userId, Long reviewId, ReplyContentDTO replyContentDTO) {

    User seller = userReader.getUserById(userId);
    ContentReview contentReview = contentReviewReader.getContentReviewById(reviewId);
    ContentReply contentReply =
        ContentReply.builder()
            .contentReview(contentReview)
            .seller(seller)
            .replyContent(replyContentDTO.getReplyContent())
            .isDeleted(false)
            .build();

    contentReplyWriter.save(contentReply);

    // 회원 리뷰인 경우에만 알림 발송 (비회원에게는 알림 불가)
    if (contentReview.isMemberReview()) {
      User buyer = contentReview.getUser();
      notificationService.sendContentReviewReplyNotification(
          buyer,
          contentReview.getContent().getId(),
          contentReview.getId(),
          contentReview.getContent().getThumbnailUrl());
    }

    return ReplyContentDTO.builder().replyContent(replyContentDTO.getReplyContent()).build();
  }

  @Transactional
  public ReplyContentDTO updateReviewReply(
      Long userId, Long reviewId, Long replyId, ReplyContentDTO replyContentDTO) {
    contentReplyWriter.updateReply(userId, reviewId, replyId, replyContentDTO.getReplyContent());
    return ReplyContentDTO.builder().replyContent(replyContentDTO.getReplyContent()).build();
  }

  @Transactional
  public void deleteReviewReply(Long userId, Long reviewId, Long replyId) {
    contentReplyWriter.deleteReply(userId, reviewId, replyId);
  }

  @Transactional(readOnly = true)
  public ContentSellDetailDTO getContentSellDetail(Long userId, Long contentId, Long purchaseId) {
    FlatContentSellDetailDTO contentSellDetailDTO =
        purchaseReader.getContentSellDetail(userId, contentId, purchaseId);

    return buildContentSellDetail(contentSellDetailDTO);
  }

  private ContentSellDetailDTO buildContentSellDetail(
      FlatContentSellDetailDTO contentSellDetailDTO) {
    return ContentSellDetailDTO.builder()
        .purchaseId(contentSellDetailDTO.getPurchaseId())
        .contentTitle(contentSellDetailDTO.getContentTitle())
        .purchasedAt(contentSellDetailDTO.getPurchasedAt())
        .purchaserNickname(contentSellDetailDTO.getPurchaserNickname())
        .purchaserEmail(contentSellDetailDTO.getPurchaserEmail())
        .purchaserPhoneNumber(contentSellDetailDTO.getPurchaserPhoneNumber())
        .selectedOptionName(contentSellDetailDTO.getSelectedOptionName())
        .finalPrice(contentSellDetailDTO.getFinalPrice())
        .paymentType(contentSellDetailDTO.getPaymentType())
        .subscriptionRound(contentSellDetailDTO.getSubscriptionRound())
        .build();
  }

  private ContentReviewDetailDTO buildContentReviewDetail(
      FlatContentReviewDetailDTO flatContentReviewDetailDTO, List<ReviewReplyDTO> reviewReplyDTOs) {
    return ContentReviewDetailDTO.builder()
        .reviewId(flatContentReviewDetailDTO.getReviewId())
        .reviewStatus(flatContentReviewDetailDTO.getReviewStatus())
        .contentTitle(flatContentReviewDetailDTO.getContentTitle())
        .createdAt(flatContentReviewDetailDTO.getCreatedAt())
        .reviewerNickname(flatContentReviewDetailDTO.getReviewerNickname())
        .reviewContent(flatContentReviewDetailDTO.getReviewContent())
        .selectedOptionName(flatContentReviewDetailDTO.getSelectedOptionName())
        .rating(flatContentReviewDetailDTO.getRating())
        .reviewReplies(reviewReplyDTOs)
        .build();
  }

  private DeleteReviewRequestReportDTO buildDeleteReviewRequestReportDTO(
      Long userId, Long reviewId) {
    return DeleteReviewRequestReportDTO.builder().userId(userId).reviewId(reviewId).build();
  }

  /** FlatContentReviewDetailDTO ContentReviewDetailDTO 변환합니다. */
  private ContentReviewDetailDTO convertFlatDTOToDetailDTO(FlatContentReviewDetailDTO flat) {
    return ContentReviewDetailDTO.builder()
        .reviewId(flat.getReviewId())
        .reviewStatus(flat.getReviewStatus())
        .contentTitle(flat.getContentTitle())
        .createdAt(flat.getCreatedAt())
        .reviewerNickname(flat.getReviewerNickname())
        .reviewContent(flat.getReviewContent())
        .selectedOptionName(flat.getSelectedOptionName())
        .rating(flat.getRating())
        .build();
  }

  /** FlatContentSellDetailDTO ContentSellDetailDTO로 변환합니다. */
  private ContentSellDetailDTO convertFlatDTOToDetailDTO(FlatContentSellDetailDTO flat) {
    log.info("▶ convertFlatDTOToDetailDTO: purchasedAt={}", flat.getPurchasedAt());

    return ContentSellDetailDTO.builder()
        .purchaseId(flat.getPurchaseId())
        .contentTitle(flat.getContentTitle())
        .purchasedAt(flat.getPurchasedAt())
        .purchaserNickname(flat.getPurchaserNickname())
        .purchaserEmail(flat.getPurchaserEmail())
        .purchaserPhoneNumber(flat.getPurchaserPhoneNumber())
        .selectedOptionName(flat.getSelectedOptionName())
        .finalPrice(flat.getFinalPrice())
        .paymentType(flat.getPaymentType())
        .subscriptionRound(flat.getSubscriptionRound())
        .build();
  }

  /** FlatContentSellDetailDTO ContentSellDetailDTO로 변환합니다. */
  private SellManageDetailDTO buildSellManageDetailDTO(
      FlatSellManageDetailDTO flatSellManageDetailDTO) {
    return SellManageDetailDTO.builder()
        .totalPaymentPrice(flatSellManageDetailDTO.getTotalPaymentPrice())
        .totalPurchaseCustomer(flatSellManageDetailDTO.getTotalPurchaseCustomer())
        .totalReviewCount(flatSellManageDetailDTO.getTotalReviewCount())
        .build();
  }
}
