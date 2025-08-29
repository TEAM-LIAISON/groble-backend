package liaison.groble.application.guest.reader;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.guest.repository.GuestUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestUserReader {
  private final GuestUserRepository guestUserRepository;

  public GuestUser getByPhoneNumber(String phoneNumber) {
    return guestUserRepository
        .findByPhoneNumber(phoneNumber)
        .orElseThrow(() -> new EntityNotFoundException("해당 전화번호의 게스트 유저를 찾을 수 없습니다."));
  }

  public boolean existsByPhoneNumber(String phoneNumber) {
    return guestUserRepository.existsByPhoneNumber(phoneNumber);
  }
}
