package liaison.groble.persistence.gig;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.gig.dto.FlatPreviewGigDTO;
import liaison.groble.domain.gig.entity.Gig;
import liaison.groble.domain.gig.repository.GigCustomRepository;
import liaison.groble.domain.gig.repository.GigRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GigRepositoryImpl implements GigRepository {
  private final JpaGigRepository jpaGigRepository;
  private final GigCustomRepository gigCustomRepository;

  @Override
  public Optional<Gig> findById(Long gigId) {
    return jpaGigRepository.findById(gigId);
  }

  @Override
  public Gig save(Gig gig) {
    return jpaGigRepository.save(gig);
  }

  public Optional<FlatPreviewGigDTO> findFlatGigById(Long gigId) {
    return gigCustomRepository.findFlatGigById(gigId);
  }

  public List<FlatPreviewGigDTO> findFlatGigsByUserId(Long userId) {
    return gigCustomRepository.findFlatGigsByUserId(userId);
  }
}
