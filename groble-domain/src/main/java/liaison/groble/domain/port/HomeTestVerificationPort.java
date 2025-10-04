package liaison.groble.domain.port;

import java.util.Optional;

import liaison.groble.domain.port.dto.HomeTestVerifiedInfo;

public interface HomeTestVerificationPort {

  void save(String token, HomeTestVerifiedInfo info, long expirationTimeInMinutes);

  Optional<HomeTestVerifiedInfo> findByToken(String token);

  Optional<HomeTestVerifiedInfo> findByPhoneNumber(String phoneNumber);

  void removeByToken(String token);

  void removeByPhoneNumber(String phoneNumber);
}
