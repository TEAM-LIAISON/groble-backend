package liaison.groble.application.auth.service;

import org.springframework.stereotype.Service;

import liaison.groble.application.user.service.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountVerificationService {

  private final UserReader userReader;
}
