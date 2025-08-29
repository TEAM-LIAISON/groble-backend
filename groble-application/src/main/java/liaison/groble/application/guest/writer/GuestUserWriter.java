package liaison.groble.application.guest.writer;

import org.springframework.stereotype.Component;

import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.guest.repository.GuestUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuestUserWriter {
  private final GuestUserRepository guestUserRepository;

  public GuestUser save(GuestUser guestUser) {
    return guestUserRepository.save(guestUser);
  }
}
