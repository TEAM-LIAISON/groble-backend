package liaison.groble.persistence.gig.initializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.gig.entity.Category;
import liaison.groble.domain.gig.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements ApplicationRunner {
  private final CategoryRepository categoryRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    log.info("Initializing categories data...");
    initializeCategoryData();
    log.info("Categories data initialization completed");
  }

  @Transactional
  protected void initializeCategoryData() {
    try {
      // 기존 데이터 조회
      List<Category> existingCategories = categoryRepository.findAll();
      Map<String, Category> existingCategoriesMap =
          existingCategories.stream()
              .collect(Collectors.toMap(category -> category.getCode(), category -> category));

      List<Category> newCategories = new ArrayList<>();
      List<Category> updatedCategories = new ArrayList<>();
      Map<String, Category> allCategoriesMap = new HashMap<>(existingCategoriesMap);

      // CSV 파일에서 데이터 읽기
      ClassPathResource resource = new ClassPathResource("data/categories.csv");
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

        // 헤더 스킵
        reader.readLine();
        String line;

        // 첫 번째 패스: 모든 카테고리 생성 또는 업데이트
        while ((line = reader.readLine()) != null) {
          String[] data = line.split(",");
          if (data.length < 6) {
            log.warn("Invalid category data format: {}", line);
            continue;
          }

          String name = data[0].trim();
          String code = data[1].trim();
          String parentCode = data[2].trim().isEmpty() ? null : data[2].trim();
          int depth = Integer.parseInt(data[3].trim());
          int displayOrder = Integer.parseInt(data[4].trim());
          boolean isActive = Boolean.parseBoolean(data[5].trim());

          if (existingCategoriesMap.containsKey(code)) {
            // 기존 데이터 업데이트 필요 여부 확인
            Category existingCategory = existingCategoriesMap.get(code);
            boolean needsUpdate =
                !Objects.equals(existingCategory.getName(), name)
                    || existingCategory.getDepth() != depth
                    || existingCategory.getDisplayOrder() != displayOrder
                    || existingCategory.isActive() != isActive;

            if (needsUpdate) {
              Category updatedCategory =
                  createCategory(
                      existingCategory.getId(), name, code, null, depth, displayOrder, isActive);
              updatedCategories.add(updatedCategory);
              allCategoriesMap.put(code, updatedCategory);
            } else {
              allCategoriesMap.put(code, existingCategory);
            }
          } else {
            // 새로운 데이터 추가
            Category newCategory =
                createCategory(null, name, code, null, depth, displayOrder, isActive);
            newCategories.add(newCategory);
            allCategoriesMap.put(code, newCategory);
          }
        }

        // 두 번째 패스: 부모-자식 관계 설정
        resource = new ClassPathResource("data/categories.csv");
        try (BufferedReader secondReader =
            new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

          // 헤더 스킵
          secondReader.readLine();

          while ((line = secondReader.readLine()) != null) {
            String[] data = line.split(",");
            if (data.length < 6) continue;

            String code = data[1].trim();
            String parentCode = data[2].trim().isEmpty() ? null : data[2].trim();

            if (parentCode != null && allCategoriesMap.containsKey(parentCode)) {
              Category child = allCategoriesMap.get(code);
              Category parent = allCategoriesMap.get(parentCode);
              setParent(child, parent);
            }
          }
        }

        // 벌크 저장/업데이트
        if (!newCategories.isEmpty()) {
          categoryRepository.saveAll(newCategories);
          log.info("Added {} new categories", newCategories.size());
        }

        if (!updatedCategories.isEmpty()) {
          categoryRepository.saveAll(updatedCategories);
          log.info("Updated {} existing categories", updatedCategories.size());
        }

        if (newCategories.isEmpty() && updatedCategories.isEmpty()) {
          log.info("Category data is already up to date");
        }
      }
    } catch (Exception e) {
      log.error("Error initializing category data", e);
      // 에러가 발생해도 애플리케이션 시작에 영향을 주지 않도록 처리
    }
  }

  private Category createCategory(
      Long id,
      String name,
      String code,
      Category parent,
      int depth,
      int displayOrder,
      boolean isActive) {
    Category category = new Category();

    if (id != null) {
      setId(category, id);
    }

    setName(category, name);
    setCode(category, code);
    setParent(category, parent);
    setDepth(category, depth);
    setDisplayOrder(category, displayOrder);
    setActive(category, isActive);

    return category;
  }

  // ID를 설정하기 위한 헬퍼 메서드
  private void setId(Category category, Long id) {
    try {
      java.lang.reflect.Field field = Category.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(category, id);
    } catch (Exception e) {
      log.error("Error setting ID field", e);
    }
  }

  // 필드 설정을 위한 헬퍼 메서드들
  private void setName(Category category, String name) {
    try {
      java.lang.reflect.Field field = Category.class.getDeclaredField("name");
      field.setAccessible(true);
      field.set(category, name);
    } catch (Exception e) {
      log.error("Error setting name field", e);
    }
  }

  private void setCode(Category category, String code) {
    try {
      java.lang.reflect.Field field = Category.class.getDeclaredField("code");
      field.setAccessible(true);
      field.set(category, code);
    } catch (Exception e) {
      log.error("Error setting code field", e);
    }
  }

  private void setParent(Category category, Category parent) {
    try {
      java.lang.reflect.Field field = Category.class.getDeclaredField("parent");
      field.setAccessible(true);
      field.set(category, parent);
    } catch (Exception e) {
      log.error("Error setting parent field", e);
    }
  }

  private void setDepth(Category category, int depth) {
    try {
      java.lang.reflect.Field field = Category.class.getDeclaredField("depth");
      field.setAccessible(true);
      field.set(category, depth);
    } catch (Exception e) {
      log.error("Error setting depth field", e);
    }
  }

  private void setDisplayOrder(Category category, int displayOrder) {
    try {
      java.lang.reflect.Field field = Category.class.getDeclaredField("displayOrder");
      field.setAccessible(true);
      field.set(category, displayOrder);
    } catch (Exception e) {
      log.error("Error setting displayOrder field", e);
    }
  }

  private void setActive(Category category, boolean isActive) {
    try {
      java.lang.reflect.Field field = Category.class.getDeclaredField("isActive");
      field.setAccessible(true);
      field.set(category, isActive);
    } catch (Exception e) {
      log.error("Error setting isActive field", e);
    }
  }
}
