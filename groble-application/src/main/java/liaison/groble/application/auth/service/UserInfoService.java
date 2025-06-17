package liaison.groble.application.auth.service;

import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.DuplicateNicknameException;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService {
  // Repository
  private final UserReader userReader;
  private final UserRepository userRepository;

  public String setNickname(Long userId, String nickname) {
    // 1) User 조회
    User user = userReader.getUserById(userId);

    // 2) 입력 정규화 (null-safe)
    String newNick = (nickname == null) ? null : nickname.strip();

    // 3) 기존 닉네임과 같으면 바로 반환
    if (Objects.equals(user.getNickname(), newNick)) {
      return user.getNickname();
    }

    // 4) 중복 검사 (새 닉네임이 null 이면 중복 검사 생략)
    if (newNick != null && userReader.isNicknameTaken(newNick, UserStatus.ACTIVE)) {
      throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
    }

    // 5) 엔티티에 반영
    user.updateNickname(newNick);

    // 6) DB 최종 유니크 제약 검사
    try {
      userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException ex) {
      throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
    }

    return user.getNickname();
  }

  @Transactional(readOnly = true)
  public boolean isNicknameTaken(String nickname) {
    return userReader.isNicknameTaken(nickname, UserStatus.ACTIVE);
  }
}
