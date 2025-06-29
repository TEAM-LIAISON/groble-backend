package liaison.groble.application.sell.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReplyWriter;
import liaison.groble.application.content.ContentReviewReader;
import liaison.groble.application.content.ContentReviewWriter;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;
import liaison.groble.domain.content.dto.FlatContentReviewDetailDTO;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;
import liaison.groble.external.discord.dto.DeleteReviewRequestReportDTO;
import liaison.groble.external.discord.service.content.DiscordDeleteReviewRequestReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellContentService {

  private final ContentReviewReader contentReviewReader;
  private final ContentReviewWriter contentReviewWriter;
  private final ContentReplyWriter contentReplyWriter;
  private final PurchaseReader purchaseReader;
  private final DiscordDeleteReviewRequestReportService discordDeleteReviewRequestReportService;

  @Transactional(readOnly = true)
  public ContentReviewDetailDTO getContentReviewDetail(Long userId, Long contentId, Long reviewId) {
    FlatContentReviewDetailDTO contentReviewDetailDTO =
        contentReviewReader.getContentReviewDetail(userId, contentId, reviewId);
    return buildContentReviewDetail(contentReviewDetailDTO);
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
    return ContentReviewDetailDTO.builder()
        .reviewId(flatContentReviewDetailDTO.getReviewId())
        .contentTitle(flatContentReviewDetailDTO.getContentTitle())
        .createdAt(flatContentReviewDetailDTO.getCreatedAt())
        .reviewerNickname(flatContentReviewDetailDTO.getReviewerNickname())
        .selectedOptionName(flatContentReviewDetailDTO.getSelectedOptionName())
        .rating(flatContentReviewDetailDTO.getRating())
        .build();
  }

  private DeleteReviewRequestReportDTO buildDeleteReviewRequestReportDTO(
      Long userId, Long reviewId) {
    return DeleteReviewRequestReportDTO.builder().userId(userId).reviewId(reviewId).build();
  }
}
