package liaison.groble.domain.purchase.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPurchaser is a Querydsl query type for Purchaser
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QPurchaser extends BeanPath<Purchaser> {

    private static final long serialVersionUID = -1271010049L;

    public static final QPurchaser purchaser = new QPurchaser("purchaser");

    public final StringPath email = createString("email");

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public QPurchaser(String variable) {
        super(Purchaser.class, forVariable(variable));
    }

    public QPurchaser(Path<? extends Purchaser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPurchaser(PathMetadata metadata) {
        super(Purchaser.class, metadata);
    }

}

