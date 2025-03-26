package liaison.grobleauth.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.grobleauth.security.jwt.JwtAuthenticationFilter;
import liaison.grobleauth.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import liaison.grobleauth.security.oauth2.OAuth2AuthenticationFailureHandler;
import liaison.grobleauth.security.oauth2.OAuth2AuthenticationSuccessHandler;
import liaison.grobleauth.security.service.UserDetailsServiceImpl;
import liaison.grobleauth.service.OAuth2AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Spring Security 설정 클래스 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserDetailsServiceImpl userDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final OAuth2AuthService oAuth2UserService;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final JwtTokenAuthenticationEntryPoint jwtTokenAuthenticationEntryPoint;
  private final JwtTokenAccessDeniedHandler jwtTokenAccessDeniedHandler;
  private final ObjectMapper objectMapper;

  /** 비밀번호 인코더 빈 설정 */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** 인증 제공자 빈 설정 */
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /** 인증 관리자 빈 설정 */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  /** CORS 설정 */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://yourdomain.com"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /** OAuth2 인증 요청 리포지토리 */
  @Bean
  public HttpCookieOAuth2AuthorizationRequestRepository
      cookieOAuth2AuthorizationRequestRepository() {
    return new HttpCookieOAuth2AuthorizationRequestRepository(objectMapper);
  }

  /** 보안 필터 체인 설정 */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // OAuth2 설정을 위한 임시 핸들러 생성 (빈으로 주입받지 않음)
    HttpCookieOAuth2AuthorizationRequestRepository requestRepository =
        new HttpCookieOAuth2AuthorizationRequestRepository(objectMapper);

    OAuth2AuthenticationFailureHandler failureHandler =
        new OAuth2AuthenticationFailureHandler(requestRepository);

    http
        // CSRF 비활성화 (REST API이므로 필요 없음)
        .csrf(AbstractHttpConfigurer::disable)

        // CORS 설정
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // 세션 관리 설정 (JWT 사용으로 STATELESS로 설정)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 예외 처리 설정
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(jwtTokenAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtTokenAccessDeniedHandler))

        // 권한 설정
        .authorizeHttpRequests(
            auth ->
                auth
                    // 인증 없이 접근 가능한 경로 설정
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/v1/oauth2/**")
                    .permitAll()
                    .requestMatchers("/oauth2/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/api/public/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    // 그 외 모든 요청은 인증 필요
                    .anyRequest()
                    .authenticated())

        // OAuth2 로그인 설정
        .oauth2Login(
            oauth2 ->
                oauth2
                    .authorizationEndpoint(
                        authorization ->
                            authorization
                                .baseUri("/oauth2/authorize")
                                .authorizationRequestRepository(
                                    cookieOAuth2AuthorizationRequestRepository()))
                    .redirectionEndpoint(redirection -> redirection.baseUri("/login/oauth2/code/*"))
                    .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(failureHandler))

        // 폼 로그인 비활성화
        .formLogin(AbstractHttpConfigurer::disable)

        // HTTP 기본 인증 비활성화
        .httpBasic(AbstractHttpConfigurer::disable)

        // JWT 필터 추가
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        // 로그아웃 설정
        .logout(withDefaults());

    // 인증 제공자 설정
    http.authenticationProvider(authenticationProvider());

    log.info("Security 설정 완료");
    return http.build();
  }
}
