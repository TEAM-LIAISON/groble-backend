package liaison.groble.domain.content.repository;

import java.util.List;

import liaison.groble.domain.content.dto.FlatReviewReplyDTO;

public interface ContentReplyCustomRepository {
  void addReply(Long userId, Long reviewId, String replyContent);

  void updateReply(Long userId, Long reviewId, Long replyId, String replyContent);

  void deleteReply(Long userId, Long reviewId, Long replyId);

  List<FlatReviewReplyDTO> findRepliesByReviewId(Long reviewId);
}
