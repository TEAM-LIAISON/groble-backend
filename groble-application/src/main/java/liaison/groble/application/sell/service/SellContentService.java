package liaison.groble.application.sell.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.ContentReplyWriter;
import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;
import liaison.groble.application.sell.dto.SellManageDetailDTO;
import liaison.groble.application.sell.dto.SellManagePageDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;
import liaison.groble.domain.purchase.dto.FlatSellManageDetailDTO;
import liaison.groble.domain.purchase.entity.Purchase;
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

  // Writer
  private final ContentReviewWriter contentReviewWriter;
  private final ContentReplyWriter contentReplyWriter;

  private final DiscordDeleteReviewRequestReportService discordDeleteReviewRequestReportService;
  private final OrderReader orderReader;
  private final ContentReader contentReader;

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
    return buildContentReviewDetail(contentReviewDetailDTO);
  }

  @Transactional(readOnly = true)
  public ContentReviewDetailDTO getContentReviewDetail(Long userId, String merchantUid) {

    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    return contentReviewReader
        .getContentReviewDetail(userId, purchase.getContent().getId())
        .map(this::buildContentReviewDetail) // 값이 있을 때만 DTO 변환
        .orElse(null); // 없으면 null 반환 (필요하면 Optional 그대로 넘겨도 OK)
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
    contentReplyWriter.addReply(userId, reviewId, replyContentDTO.getReplyContent());
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
        .build();
  }

  private ContentReviewDetailDTO buildContentReviewDetail(
      FlatContentReviewDetailDTO flatContentReviewDetailDTO) {
    log.info(flatContentReviewDetailDTO.toString());
    return ContentReviewDetailDTO.builder()
        .reviewId(flatContentReviewDetailDTO.getReviewId())
        .contentTitle(flatContentReviewDetailDTO.getContentTitle())
        .createdAt(flatContentReviewDetailDTO.getCreatedAt())
        .reviewerNickname(flatContentReviewDetailDTO.getReviewerNickname())
        .reviewContent(flatContentReviewDetailDTO.getReviewContent())
        .selectedOptionName(flatContentReviewDetailDTO.getSelectedOptionName())
        .rating(flatContentReviewDetailDTO.getRating())
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
        .contentTitle(flat.getContentTitle())
        .createdAt(flat.getCreatedAt())
        .reviewerNickname(flat.getReviewerNickname())
        .selectedOptionName(flat.getSelectedOptionName())
        .rating(flat.getRating())
        .build();
  }

  /** FlatContentSellDetailDTO ContentSellDetailDTO로 변환합니다. */
  private ContentSellDetailDTO convertFlatDTOToDetailDTO(FlatContentSellDetailDTO flat) {
    return ContentSellDetailDTO.builder()
        .purchaseId(flat.getPurchaseId())
        .contentTitle(flat.getContentTitle())
        .purchasedAt(flat.getPurchasedAt())
        .purchaserNickname(flat.getPurchaserNickname())
        .purchaserEmail(flat.getPurchaserEmail())
        .purchaserPhoneNumber(flat.getPurchaserPhoneNumber())
        .selectedOptionName(flat.getSelectedOptionName())
        .finalPrice(flat.getFinalPrice())
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
