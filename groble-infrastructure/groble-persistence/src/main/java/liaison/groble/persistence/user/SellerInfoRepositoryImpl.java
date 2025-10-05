package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.repository.SellerInfoRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SellerInfoRepositoryImpl implements SellerInfoRepository {
  private final JpaSellerInfoRepository jpaSellerInfoRepository;

  @Override
  public SellerInfo save(SellerInfo sellerInfo) {
    return jpaSellerInfoRepository.save(sellerInfo);
  }

  @Override
  public SellerInfo saveAndFlush(SellerInfo sellerInfo) {
    return jpaSellerInfoRepository.saveAndFlush(sellerInfo);
  }

  @Override
  public Optional<SellerInfo> findByUserId(Long userId) {
    return jpaSellerInfoRepository.findByUserId(userId);
  }

  @Override
  public Optional<SellerInfo> findByUserIdWithUser(Long userId) {
    return jpaSellerInfoRepository.findByUserIdWithUser(userId);
  }

  @Override
  public Optional<SellerInfo> findByUserNicknameWithUser(String nickname) {
    return jpaSellerInfoRepository.findByUserNicknameWithUser(nickname);
  }

  @Override
  public boolean existsByUserId(Long userId) {
    return jpaSellerInfoRepository.existsByUserId(userId);
  }
}
