package liaison.groble.domain.content.repository;

public interface ContentReplyCustomRepository {
  void addReply(Long userId, Long reviewId, String replyContent);

  void updateReply(Long userId, Long reviewId, Long replyId, String replyContent);

  void deleteReply(Long userId, Long reviewId, Long replyId);
}
