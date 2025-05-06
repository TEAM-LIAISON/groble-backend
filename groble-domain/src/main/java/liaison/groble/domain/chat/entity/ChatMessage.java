// package liaison.groble.domain.chat.entity;
//
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
//
// import liaison.groble.domain.common.entity.BaseTimeEntity;
// import liaison.groble.domain.user.entity.User;
//
// import lombok.AccessLevel;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
//
/// ** 채팅 메시지 엔티티 채팅방 내의 개별 메시지를 저장 */
// @Entity
// @Getter
// @NoArgsConstructor(access = AccessLevel.PROTECTED)
// public class ChatMessage extends BaseTimeEntity {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long id;
//
//  // 채팅방 관계
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "chat_room_id")
//  private ChatRoom chatRoom;
//
//  // 메시지 작성자
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "sender_id")
//  private User sender;
//
//  // 메시지 유형 (텍스트, 이미지, 시스템 메시지 등)
//  @Enumerated(EnumType.STRING)
//  private MessageType type;
//
//  // 메시지 내용
//  @Column(columnDefinition = "TEXT")
//  private String content;
//
//  // 메시지가 읽혔는지 여부
//  private boolean read = false;
//
//  // 메시지 삭제 여부 (소프트 삭제)
//  private boolean deleted = false;
//
//  @Builder
//  public ChatMessage(ChatRoom chatRoom, User sender, MessageType type, String content) {
//    this.chatRoom = chatRoom;
//    this.sender = sender;
//    this.type = type;
//    this.content = content;
//  }
//
//  // 메시지 읽음 표시
//  public void markAsRead() {
//    this.read = true;
//  }
//
//  // 메시지 소프트 삭제
//  public void delete() {
//    this.deleted = true;
//    this.content = "삭제된 메시지입니다.";
//  }
// }
