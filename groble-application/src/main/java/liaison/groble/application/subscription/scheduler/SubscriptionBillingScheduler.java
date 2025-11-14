package liaison.groble.application.subscription.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import liaison.groble.application.subscription.service.SubscriptionBillingJobService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionBillingScheduler {

  private final SubscriptionBillingJobService billingJobService;

  @Scheduled(cron = "${subscription.billing.cron:0 */1 * * * ?}", zone = "Asia/Seoul")
  public void runSubscriptionBilling() {
    log.debug("정기결제 자동 청구 스케줄러 실행");
    billingJobService.processDueSubscriptions();
  }
}
