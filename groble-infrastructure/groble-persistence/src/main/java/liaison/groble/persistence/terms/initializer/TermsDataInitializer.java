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

import liaison.groble.domain.terms.entity.Terms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.terms.repository.TermsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TermsDataInitializer implements ApplicationRunner {
  private final TermsRepository termsRepository;
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    log.info("Initializing terms data...");
    initializeTermsData();
    log.info("Terms data initialization completed");
  }

  @Transactional
  protected void initializeTermsData() {
    try {
      // 기존 데이터 조회
      List<Terms> existingTerms = termsRepository.findAll();
      Map<String, Terms> existingTermsMap =
          existingTerms.stream()
              .collect(
                  Collectors.toMap(
                      terms -> terms.getType() + "_" + terms.getVersion(), terms -> terms));

      List<Terms> newTerms = new ArrayList<>();
      List<Terms> updatedTerms = new ArrayList<>();

      // CSV 파일에서 데이터 읽기
      ClassPathResource resource = new ClassPathResource("data/terms.csv");
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

        // 헤더 스킵
        reader.readLine();
        String line;

        while ((line = reader.readLine()) != null) {
          String[] data = line.split(",");
          if (data.length < 5) {
            log.warn("Invalid terms data format: {}", line);
            continue;
          }

          String title = data[0].trim();
          TermsType type = TermsType.valueOf(data[1].trim());
          String version = data[2].trim();
          String contentUrl = data[3].trim();
          LocalDateTime effectiveFrom = LocalDateTime.parse(data[4].trim(), DATE_TIME_FORMATTER);
          LocalDateTime effectiveTo =
              data.length > 5 && !data[5].trim().isEmpty()
                  ? LocalDateTime.parse(data[5].trim(), DATE_TIME_FORMATTER)
                  : null;

          String key = type + "_" + version;

          if (existingTermsMap.containsKey(key)) {
            // 기존 데이터 업데이트 필요 여부 확인
            Terms existingTerm = existingTermsMap.get(key);
            boolean needsUpdate =
                !Objects.equals(existingTerm.getTitle(), title)
                    || !Objects.equals(existingTerm.getContentUrl(), contentUrl)
                    || !Objects.equals(existingTerm.getEffectiveFrom(), effectiveFrom)
                    || !Objects.equals(existingTerm.getEffectiveTo(), effectiveTo);

            if (needsUpdate) {
              Terms updatedTerm =
                  Terms.builder()
                      .title(title)
                      .type(type)
                      .version(version)
                      .contentUrl(contentUrl)
                      .effectiveFrom(effectiveFrom)
                      .build();

              if (effectiveTo != null) {
                updatedTerm.updateEffectiveTo(effectiveTo);
              }

              updatedTerm = setId(updatedTerm, existingTerm.getId());
              updatedTerms.add(updatedTerm);
            }
          } else {
            // 새로운 데이터 추가
            Terms newTerm =
                Terms.builder()
                    .title(title)
                    .type(type)
                    .version(version)
                    .contentUrl(contentUrl)
                    .effectiveFrom(effectiveFrom)
                    .build();

            if (effectiveTo != null) {
              newTerm.updateEffectiveTo(effectiveTo);
            }

            newTerms.add(newTerm);
          }
        }

        // 벌크 저장/업데이트
        if (!newTerms.isEmpty()) {
          termsRepository.saveAll(newTerms);
          log.info("Added {} new terms", newTerms.size());
        }

        if (!updatedTerms.isEmpty()) {
          termsRepository.saveAll(updatedTerms);
          log.info("Updated {} existing terms", updatedTerms.size());
        }

        if (newTerms.isEmpty() && updatedTerms.isEmpty()) {
          log.info("Terms data is already up to date");
        }
      }
    } catch (Exception e) {
      log.error("Error initializing terms data", e);
      // 에러가 발생해도 애플리케이션 시작에 영향을 주지 않도록 처리
    }
  }

  // ID를 설정하기 위한 헬퍼 메서드 (리플렉션이나 다른 방법으로 대체 가능)
  private Terms setId(Terms terms, Long id) {
    try {
      java.lang.reflect.Field field = Terms.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(terms, id);
      return terms;
    } catch (Exception e) {
      log.error("Error setting ID field", e);
      return terms;
    }
  }
}
