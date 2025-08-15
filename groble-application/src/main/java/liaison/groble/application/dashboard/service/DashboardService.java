package liaison.groble.application.dashboard.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.dashboard.dto.DashboardOverviewDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
  // Reader

  @Transactional(readOnly = true)
  public DashboardOverviewDTO getDashboardOverview(Long userId) {
    return null;
  }
}
