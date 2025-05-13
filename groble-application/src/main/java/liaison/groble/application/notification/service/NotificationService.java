package liaison.groble.application.notification.service;

import org.springframework.stereotype.Service;

import liaison.groble.domain.notification.repository.NotificationCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationCustomRepository notificationCustomRepository;
}
