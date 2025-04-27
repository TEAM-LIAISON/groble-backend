package liaison.groble.domain.gig.repository;

import java.util.Optional;

import liaison.groble.domain.gig.entity.Gig;

public interface GigRepository {
  Optional<Gig> findById(Long gigId);

  Gig save(Gig gig);
}
