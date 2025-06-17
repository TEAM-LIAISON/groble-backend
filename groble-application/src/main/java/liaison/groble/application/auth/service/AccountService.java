package liaison.groble.application.auth.service;

import org.springframework.stereotype.Service;

import liaison.groble.application.user.service.UserReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
  // Repository
  private final UserReader userReader;
}
