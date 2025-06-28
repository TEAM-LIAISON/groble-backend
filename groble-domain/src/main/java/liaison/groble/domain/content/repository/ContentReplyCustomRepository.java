package liaison.groble.domain.content.repository;

public interface ContentReplyCustomRepository {
  void addReply(Long userId, Long reviewId, String replyContent);
}
