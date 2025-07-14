package liaison.groble.application.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

  /** 1) 앱 내 일반 비동기 작업용 Executor - DAU 1,000 기준으로 가벼운 백그라운드 작업(로그 처리, 통계 집계 등) */
  @Bean
  public ThreadPoolTaskExecutor defaultAsyncExecutor() {
    ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
    exec.setCorePoolSize(4); // 평상시 4스레드 유지
    exec.setMaxPoolSize(10); // 피크 시 최대 10스레드
    exec.setQueueCapacity(500); // 최대 500개 대기
    exec.setThreadNamePrefix("app-async-"); // 스레드명 접두어
    exec.setRejectedExecutionHandler(
        new ThreadPoolExecutor.CallerRunsPolicy() // 큐 만료 시 호출 스레드에서 실행
        );
    exec.initialize();
    return exec;
  }

  /**
   * 2) 이메일 전송 전용 Executor - SMTP 왕복 200~300ms + 렌더링 오버헤드 ~50ms 가정 - core 2 스레드로 약 5TPS, max 5 스레드로
   * 약 12TPS 처리 가능
   */
  @Bean(name = "mailExecutor")
  public ThreadPoolTaskExecutor mailExecutor() {
    ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
    exec.setCorePoolSize(2); // 평상시 2스레드
    exec.setMaxPoolSize(5); // 피크 시 최대 5스레드
    exec.setQueueCapacity(200); // 최대 200개 대기 (≈40초 버퍼)
    exec.setThreadNamePrefix("mail-async-"); // 메일 전용 스레드명
    exec.setWaitForTasksToCompleteOnShutdown(true);
    exec.setAwaitTerminationSeconds(30); // 종료 시 최대 30초 대기
    exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    exec.initialize();
    return exec;
  }

  /**
   * @Async 애노테이션이 아무 값 없이 호출될 때 사용하는 기본 Executor
   */
  @Override
  public Executor getAsyncExecutor() {
    return defaultAsyncExecutor();
  }

  /**
   * @Async 메서드에서 발생하는 예외를 처리할 핸들러
   */
  @Override
  public SimpleAsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new SimpleAsyncUncaughtExceptionHandler();
  }
}
