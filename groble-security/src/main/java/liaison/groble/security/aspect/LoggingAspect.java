package liaison.groble.security.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import liaison.groble.common.annotation.Logging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAspectJAutoProxy
@Component
public class LoggingAspect {

  @Around("@annotation(liaison.groble.common.annotation.Logging)")
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    Logging loggingAnnotation = method.getAnnotation(Logging.class);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    String item = loggingAnnotation.item();
    String action = loggingAnnotation.action();

    // 요청 정보 로깅
    if (loggingAnnotation.includeParam()) {
      log.info(
          "[{}] {} started - Parameters: {}", item, action, Arrays.toString(joinPoint.getArgs()));
    } else {
      log.info("[{}] {} started", item, action);
    }

    try {
      Object result = joinPoint.proceed();
      stopWatch.stop();

      // 응답 정보 로깅
      if (loggingAnnotation.includeResult()) {
        log.info(
            "[{}] {} completed in {}ms - Result: {}",
            item,
            action,
            stopWatch.getTotalTimeMillis(),
            result);
      } else {
        log.info("[{}] {} completed in {}ms", item, action, stopWatch.getTotalTimeMillis());
      }

      return result;
    } catch (Exception e) {
      stopWatch.stop();
      log.error(
          "[{}] {} failed in {}ms - Error: {}",
          item,
          action,
          stopWatch.getTotalTimeMillis(),
          e.getMessage());
      throw e;
    }
  }
}
