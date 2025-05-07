package liaison.groble.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;

/** QChatRoom is a Querydsl query type for ChatRoom */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatRoom extends EntityPathBase<ChatRoom> {

  private static final long serialVersionUID = 1170997116L;

  private static final PathInits INITS = PathInits.DIRECT2;

  public static final QChatRoom chatRoom = new QChatRoom("chatRoom");

  public final liaison.groble.domain.common.entity.QBaseTimeEntity _super =
      new liaison.groble.domain.common.entity.QBaseTimeEntity(this);

  public final liaison.groble.domain.user.entity.QUser buyer;

  public final NumberPath<Integer> buyerUnreadCount =
      createNumber("buyerUnreadCount", Integer.class);

  public final liaison.groble.domain.content.entity.QContent content;

  // inherited
  public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath lastMessage = createString("lastMessage");

  public final DateTimePath<java.time.Instant> lastMessageTime =
      createDateTime("lastMessageTime", java.time.Instant.class);

  // inherited
  public final DateTimePath<java.time.Instant> modifiedAt = _super.modifiedAt;

  public final liaison.groble.domain.user.entity.QUser seller;

  public final NumberPath<Integer> sellerUnreadCount =
      createNumber("sellerUnreadCount", Integer.class);

  public final EnumPath<liaison.groble.domain.chat.enums.ChatRoomStatus> status =
      createEnum("status", liaison.groble.domain.chat.enums.ChatRoomStatus.class);

  public final StringPath title = createString("title");

  public QChatRoom(String variable) {
    this(ChatRoom.class, forVariable(variable), INITS);
  }

  public QChatRoom(Path<? extends ChatRoom> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QChatRoom(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QChatRoom(PathMetadata metadata, PathInits inits) {
    this(ChatRoom.class, metadata, inits);
  }

  public QChatRoom(Class<? extends ChatRoom> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.buyer =
        inits.isInitialized("buyer")
            ? new liaison.groble.domain.user.entity.QUser(forProperty("buyer"), inits.get("buyer"))
            : null;
    this.content =
        inits.isInitialized("content")
            ? new liaison.groble.domain.content.entity.QContent(
                forProperty("content"), inits.get("content"))
            : null;
    this.seller =
        inits.isInitialized("seller")
            ? new liaison.groble.domain.user.entity.QUser(
                forProperty("seller"), inits.get("seller"))
            : null;
  }
}
