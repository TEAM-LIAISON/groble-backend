package liaison.groble.persistence.gig;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.gig.entity.Gig;

public interface JpaGigRepository extends JpaRepository<Gig, Long> {}
