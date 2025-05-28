package liaison.groble.persistence.terms.initializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.terms.entity.OrderTerms;
import liaison.groble.domain.terms.entity.Terms;
import liaison.groble.domain.terms.enums.OrderTermsType;
import liaison.groble.domain.terms.repository.OrderTermsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTermsDataInitializer implements ApplicationRunner {
  private final OrderTermsRepository orderTermsRepository;
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    log.info("Initializing order_terms data...");
    initializeOrderTermsData();
    log.info("Order Terms data initialization completed");
  }

  @Transactional
  protected void initializeOrderTermsData() {
    try {
      // 기존 데이터 조회
      List<OrderTerms> existingOrderTerms = orderTermsRepository.findAll();
      Map<String, OrderTerms> existingOrderTermsMap =
          existingOrderTerms.stream()
              .collect(
                  Collectors.toMap(
                      orderTerms -> orderTerms.getType() + "_" + orderTerms.getVersion(),
                      orderTerms -> orderTerms));

      List<OrderTerms> newOrderTerms = new ArrayList<>();
      List<OrderTerms> updatedOrderTerms = new ArrayList<>();

      // CSV 파일에서 데이터 읽기
      ClassPathResource resource = new ClassPathResource("data/orderTerms.csv");
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

        // 헤더 스킵
        reader.readLine();
        String line;

        while ((line = reader.readLine()) != null) {
          String[] data = line.split(",");
          if (data.length < 5) {
            log.warn("Invalid order terms data format: {}", line);
            continue;
          }

          String title = data[0].trim();
          OrderTermsType type = OrderTermsType.valueOf(data[1].trim());
          String version = data[2].trim();
          String contentUrl = data[3].trim();
          LocalDateTime effectiveFrom = LocalDateTime.parse(data[4].trim(), DATE_TIME_FORMATTER);
          LocalDateTime effectiveTo =
              data.length > 5 && !data[5].trim().isEmpty()
                  ? LocalDateTime.parse(data[5].trim(), DATE_TIME_FORMATTER)
                  : null;

          String key = type + "_" + version;

          if (existingOrderTermsMap.containsKey(key)) {
            // 기존 데이터 업데이트 필요 여부 확인
            OrderTerms existingOrderTerm = existingOrderTermsMap.get(key);
            boolean needsUpdate =
                !Objects.equals(existingOrderTerm.getTitle(), title)
                    || !Objects.equals(existingOrderTerm.getContentUrl(), contentUrl)
                    || !Objects.equals(existingOrderTerm.getEffectiveFrom(), effectiveFrom)
                    || !Objects.equals(existingOrderTerm.getEffectiveTo(), effectiveTo);

            if (needsUpdate) {
              OrderTerms updatedOrderTerm =
                  OrderTerms.builder()
                      .title(title)
                      .type(type)
                      .version(version)
                      .contentUrl(contentUrl)
                      .effectiveFrom(effectiveFrom)
                      .build();

              if (effectiveTo != null) {
                updatedOrderTerm.updateEffectiveTo(effectiveTo);
              }

              updatedOrderTerm = setId(updatedOrderTerm, existingOrderTerm.getId());
              updatedOrderTerms.add(updatedOrderTerm);
            }
          } else {
            // 새로운 데이터 추가
            OrderTerms newOrderTerm =
                OrderTerms.builder()
                    .title(title)
                    .type(type)
                    .version(version)
                    .contentUrl(contentUrl)
                    .effectiveFrom(effectiveFrom)
                    .build();

            if (effectiveTo != null) {
              newOrderTerm.updateEffectiveTo(effectiveTo);
            }

            newOrderTerms.add(newOrderTerm);
          }
        }

        // 벌크 저장/업데이트
        if (!newOrderTerms.isEmpty()) {
          orderTermsRepository.saveAll(newOrderTerms);
          log.info("Added {} new terms", newOrderTerms.size());
        }

        if (!updatedOrderTerms.isEmpty()) {
          orderTermsRepository.saveAll(updatedOrderTerms);
          log.info("Updated {} existing order terms", updatedOrderTerms.size());
        }

        if (newOrderTerms.isEmpty() && updatedOrderTerms.isEmpty()) {
          log.info("Order Terms data is already up to date");
        }
      }
    } catch (Exception e) {
      log.error("Error initializing order terms data", e);
      // 에러가 발생해도 애플리케이션 시작에 영향을 주지 않도록 처리
    }
  }

  // ID를 설정하기 위한 헬퍼 메서드 (리플렉션이나 다른 방법으로 대체 가능)
  private OrderTerms setId(OrderTerms orderTerms, Long id) {
    try {
      java.lang.reflect.Field field = Terms.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(orderTerms, id);
      return orderTerms;
    } catch (Exception e) {
      log.error("Error setting ID field", e);
      return orderTerms;
    }
  }
}
