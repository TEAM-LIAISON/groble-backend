package liaison.groble.domain.port;

import java.util.Optional;

import liaison.groble.domain.port.dto.HomeTestVerifiedInfo;

public interface HomeTestVerificationPort {
  void save(HomeTestVerifiedInfo info, long expirationTimeInMinutes);

  Optional<HomeTestVerifiedInfo> findByPhoneNumber(String phoneNumber);

  void remove(String phoneNumber);
}
