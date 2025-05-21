package liaison.groble.domain.user.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;

/** QUserProfile is a Querydsl query type for UserProfile */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QUserProfile extends BeanPath<UserProfile> {

  private static final long serialVersionUID = 1803450776L;

  public static final QUserProfile userProfile = new QUserProfile("userProfile");

  public final StringPath nickname = createString("nickname");

  public final StringPath phoneNumber = createString("phoneNumber");

  public final StringPath profileImageUrl = createString("profileImageUrl");

  public QUserProfile(String variable) {
    super(UserProfile.class, forVariable(variable));
  }

  public QUserProfile(Path<? extends UserProfile> path) {
    super(path.getType(), path.getMetadata());
  }

  public QUserProfile(PathMetadata metadata) {
    super(UserProfile.class, metadata);
  }
}
