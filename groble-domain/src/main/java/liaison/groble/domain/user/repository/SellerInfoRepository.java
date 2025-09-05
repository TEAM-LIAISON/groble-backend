package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.SellerInfo;

public interface SellerInfoRepository {
  // 판매자 정보 저장
  SellerInfo save(SellerInfo sellerInfo);

  Optional<SellerInfo> findByUserId(Long userId);

  Optional<SellerInfo> findByUserIdWithUser(Long userId);

  Optional<SellerInfo> findByUserNicknameWithUser(String nickname);

  // 중복 생성 방지를 위한 존재 여부 확인
  boolean existsByUserId(Long userId);
}
