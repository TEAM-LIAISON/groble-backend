package liaison.groble.mapping.config;

import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/** MapStruct 공통 설정 모든 Mapper가 이 설정을 상속받아 일관된 매핑 규칙을 적용합니다. */
@MapperConfig(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR, // 생성자 주입 사용
    unmappedTargetPolicy = ReportingPolicy.ERROR, // 매핑되지 않은 필드가 있으면 컴파일 에러
    unmappedSourcePolicy = ReportingPolicy.WARN, // 사용되지 않은 소스 필드는 경고
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, // null 체크 항상 수행
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, // null 값은 매핑하지 않음
    builder = @Builder(disableBuilder = false) // Builder 패턴 활성화
    )
public interface GrobleMapperConfig {}
