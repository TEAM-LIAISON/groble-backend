package liaison.groble.application.user.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MakerReader {
  private final UserRepository userRepository;

  public User getUserByMarketLinkUrl(String marketLinkUrl) {
    return userRepository
        .findBySellerInfoMarketLinkUrl(marketLinkUrl)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "해당 마켓 URL을 가진 메이커를 찾을 수 없습니다. 마켓 링크 URL : " + marketLinkUrl));
  }
}
