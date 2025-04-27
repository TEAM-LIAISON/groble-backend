package liaison.groble.persistence.gig;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.gig.entity.Gig;
import liaison.groble.domain.gig.repository.GigRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class GigRepositoryImpl implements GigRepository {
  private final JpaGigRepository jpaGigRepository;

  @Override
  public Optional<Gig> findById(Long gigId) {
    return jpaGigRepository.findById(gigId);
  }

  @Override
  public Gig save(Gig gig) {
    return jpaGigRepository.save(gig);
  }
}
