package liaison.groble.api.server.auth.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.SignupRequest;
import liaison.groble.api.model.auth.response.SignupResponse;
import liaison.groble.application.auth.dto.SignupDto;

@Component
public class AuthDtoMapper {
  /** API 요청 DTO를 서비스 레이어 DTO로 변환 */
  public SignupDto toServiceDto(SignupRequest request) {
    return SignupDto.builder().email(request.getEmail()).password(request.getPassword()).build();
  }

  /** 서비스 레이어에서 API 응답 DTO로 변환 */
  public SignupResponse toResponseDto(String email) {
    return SignupResponse.of(email);
  }
}
