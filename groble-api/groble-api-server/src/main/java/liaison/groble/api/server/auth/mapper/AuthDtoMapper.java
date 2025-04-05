package liaison.groble.api.server.auth.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.auth.request.SignInRequest;
import liaison.groble.api.model.auth.request.SignUpRequest;
import liaison.groble.api.model.auth.response.SignUpResponse;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;

@Component
public class AuthDtoMapper {
  /** API 요청 DTO를 서비스 레이어 DTO로 변환 */
  public SignUpDto toServiceSignUpDto(SignUpRequest request) {
    return SignUpDto.builder().email(request.getEmail()).password(request.getPassword()).build();
  }

  /** 서비스 레이어에서 API 응답 DTO로 변환 */
  public SignUpResponse toResponseDto(String email) {
    return SignUpResponse.of(email);
  }

  public SignInDto toServiceSignInDto(SignInRequest request) {
    return SignInDto.builder().email(request.getEmail()).password(request.getPassword()).build();
  }
}
