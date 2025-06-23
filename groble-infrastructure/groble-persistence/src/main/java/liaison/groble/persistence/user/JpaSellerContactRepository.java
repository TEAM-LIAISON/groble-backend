package liaison.groble.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.SellerContact;

public interface JpaSellerContactRepository extends JpaRepository<SellerContact, Long> {}
