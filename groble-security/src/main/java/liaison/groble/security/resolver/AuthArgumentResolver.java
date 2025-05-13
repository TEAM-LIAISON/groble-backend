package liaison.groble.security.resolver;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.security.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(Auth.class)
        && parameter.getParameterType().equals(Accessor.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Auth authAnnotation = parameter.getParameterAnnotation(Auth.class);
    boolean isRequired = authAnnotation.required();

    // 인증되지 않은 경우
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {

      if (isRequired) {
        throw new UnauthorizedException("인증 정보가 없습니다.");
      }

      // required=false일 경우 null 반환
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof liaison.groble.security.jwt.UserDetailsImpl)) {
      if (isRequired) {
        throw new UnauthorizedException("인증 정보가 유효하지 않습니다.");
      }
      return null;
    }

    // 기존 로직과 동일
    liaison.groble.security.jwt.UserDetailsImpl userDetails =
        (liaison.groble.security.jwt.UserDetailsImpl) principal;

    Set<String> roles =
        userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    String userType = "BUYER"; // 기본값
    if (roles.contains("ROLE_SELLER")) {
      userType = userDetails.getLastUserType() != null ? userDetails.getLastUserType() : "BUYER";
    }

    String accountType = userDetails.getAccountType();

    return Accessor.builder()
        .id(userDetails.getId())
        .email(userDetails.getEmail())
        .roles(roles)
        .userType(userType)
        .accountType(accountType)
        .build();
  }
}
