package liaison.groble.domain.chat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private User seller;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id")
  private User buyer;

  // 마지막 메시지 내용 (채팅방 목록에서 미리보기 용도)
  private String lastMessage;

  // 마지막 메시지 전송 시간
  private LocalDateTime lastMessageTime;

  // 채팅방 활성화 상태
  private boolean active = true;

  @Builder
  public ChatRoom(User seller, User buyer, String title) {
    this.seller = seller;
    this.buyer = buyer;
  }

  // 마지막 메시지 업데이트
  public void updateLastMessage(String message, LocalDateTime time) {
    this.lastMessage = message;
    this.lastMessageTime = time;
  }

  // 채팅방 비활성화
  public void deactivate() {
    this.active = false;
  }
}
