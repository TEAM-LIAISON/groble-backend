package liaison.groble.application.user.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.market.repository.MarketRepository;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.SellerInfoRepository;
import liaison.groble.domain.user.repository.SocialAccountRepository;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 사용자 조회 담당 전용 컴포넌트 모든 사용자 조회 로직을 중앙화하여 일관성 있는 조회 및 예외 처리를 제공 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {
  // 주입 Repository
  private final UserRepository userRepository;
  private final IntegratedAccountRepository integratedAccountRepository;
  private final SocialAccountRepository socialAccountRepository;
  private final SellerInfoRepository sellerInfoRepository;
  private final MarketRepository marketRepository;

  // ===== ID로 User 조회 =====
  public User getUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
  }

  // userId로 SellerInfo 조회 (User fetch join)
  public SellerInfo getSellerInfoWithUser(Long userId) {
    return sellerInfoRepository
        .findByUserIdWithUser(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자의 판매자 정보를 찾을 수 없습니다."));
  }

  public SellerInfo getSellerInfoWithUser(String nickname) {
    return sellerInfoRepository
        .findByUserNicknameWithUser(nickname)
        .orElseThrow(() -> new EntityNotFoundException("해당 닉네임을 가진 사용자의 판매자 정보를 찾을 수 없습니다."));
  }

  public SellerInfo getSellerInfo(Long userId) {
    return sellerInfoRepository
        .findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자의 판매자 정보를 찾을 수 없습니다."));
  }

  public Market getMarketWithUser(String marketLinkUrl) {
    return marketRepository
        .findByMarketLinkUrl(marketLinkUrl)
        .orElseThrow(() -> new EntityNotFoundException("해당 마켓 링크 URL을 가진 마켓 정보를 찾을 수 없습니다."));
  }

  // userId로 Market 조회
  public Market getMarket(Long userId) {
    return marketRepository
        .findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자의 마켓 정보를 찾을 수 없습니다. ID: " + userId));
  }

  // ===== 닉네임으로 User 조회 =====

  public User getUserByNickname(String nickname) {
    return userRepository
        .findByNickname(nickname)
        .orElseThrow(
            () -> new EntityNotFoundException("해당 닉네임을 가진 사용자를 찾을 수 없습니다. 닉네임: " + nickname));
  }

  /**
   * 닉네임 중복 확인
   *
   * @param nickname 확인할 닉네임
   * @return 닉네임 사용 여부 (true: 사용 중, false: 사용 가능)
   */
  public boolean isNicknameTaken(String nickname, UserStatus userStatus) {
    return userRepository.existsByNicknameAndStatus(nickname, userStatus);
  }

  // ===== 이메일로 User 조회 =====
  public IntegratedAccount getUserByIntegratedAccountEmail(String email) {
    return integratedAccountRepository
        .findByIntegratedAccountEmail(email)
        .orElseThrow(
            () -> new EntityNotFoundException("해당 통합 계정용 이메일로 가입한 사용자를 찾을 수 없습니다. 이메일: " + email));
  }

  // ===== 사용자 존재 여부 확인 =====

  public boolean existsByIntegratedAccountEmail(String email) {
    return integratedAccountRepository.existsByIntegratedAccountEmail(email);
  }

  public boolean existsBySocialAccountEmail(String email) {
    return socialAccountRepository.existsBySocialAccountEmail(email);
  }

  public boolean existsByMarketLinkUrl(String marketLinkUrl) {
    return marketRepository.existsByMarketLinkUrl(marketLinkUrl);
  }
}
