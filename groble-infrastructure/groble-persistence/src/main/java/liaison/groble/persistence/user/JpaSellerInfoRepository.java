package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.user.entity.SellerInfo;

public interface JpaSellerInfoRepository extends JpaRepository<SellerInfo, Long> {
  Optional<SellerInfo> findByUserId(Long userId);

  @Query("SELECT s FROM SellerInfo s JOIN FETCH s.user WHERE s.user.id = :userId")
  Optional<SellerInfo> findByUserIdWithUser(@Param("userId") Long userId);

  @Query("SELECT s FROM SellerInfo s JOIN FETCH s.user u WHERE u.userProfile.nickname = :nickname")
  Optional<SellerInfo> findByUserNicknameWithUser(@Param("nickname") String nickname);
}
