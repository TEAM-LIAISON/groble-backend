package liaison.groble.domain.chat.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import liaison.groble.domain.chat.enums.ChatRoomStatus;
import liaison.groble.domain.common.entity.BaseTimeEntity;
import liaison.groble.domain.content.entity.Content;
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

  private String title; // 채팅방 제목 추가

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private User seller;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id")
  private User buyer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id")
  private Content content; // 관련 콘텐츠 추가

  // 채팅방 상태 (ACTIVE, INACTIVE, DELETED 등)
  @Enumerated(EnumType.STRING)
  private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

  // 마지막 메시지 내용 (채팅방 목록에서 미리보기 용도)
  private String lastMessage;

  // 마지막 메시지 전송 시간
  private Instant lastMessageTime;

  // 판매자 읽지 않은 메시지 수
  private int sellerUnreadCount = 0;

  // 구매자 읽지 않은 메시지 수
  private int buyerUnreadCount = 0;

  @Builder
  public ChatRoom(User seller, User buyer, Content content, String title) {
    this.seller = seller;
    this.buyer = buyer;
    this.content = content;
    this.title = title;
  }

  // 마지막 메시지 업데이트
  public void updateLastMessage(String message, Instant time) {
    this.lastMessage = message;
    this.lastMessageTime = time;
  }

  // 사용자가 채팅방 참여자인지 확인
  public boolean isParticipant(User user) {
    return user.getId().equals(seller.getId()) || user.getId().equals(buyer.getId());
  }

  // 판매자 메시지 읽음 처리
  public void markAsReadBySeller() {
    this.sellerUnreadCount = 0;
  }

  // 구매자 메시지 읽음 처리
  public void markAsReadByBuyer() {
    this.buyerUnreadCount = 0;
  }

  // 채팅방 상태 변경 (비활성화, 삭제 등)
  public void updateStatus(ChatRoomStatus status) {
    this.status = status;
  }
}
