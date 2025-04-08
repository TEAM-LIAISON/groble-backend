package liaison.groble.security.config;

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
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.security.jwt.JwtAuthenticationFilter;
import liaison.groble.security.jwt.UserDetailsServiceImpl;
import liaison.groble.security.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import liaison.groble.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;

import lombok.extern.slf4j.Slf4j;

/** Spring Security 설정 클래스 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final UserDetailsServiceImpl userDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final JwtTokenAuthenticationEntryPoint jwtTokenAuthenticationEntryPoint;
  private final JwtTokenAccessDeniedHandler jwtTokenAccessDeniedHandler;
  private final ObjectMapper objectMapper;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

  public SecurityConfig(
      UserDetailsServiceImpl userDetailsService,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
      JwtTokenAuthenticationEntryPoint jwtTokenAuthenticationEntryPoint,
      JwtTokenAccessDeniedHandler jwtTokenAccessDeniedHandler,
      ObjectMapper objectMapper,
      OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService) {
    this.userDetailsService = userDetailsService;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    this.jwtTokenAuthenticationEntryPoint = jwtTokenAuthenticationEntryPoint;
    this.jwtTokenAccessDeniedHandler = jwtTokenAccessDeniedHandler;
    this.objectMapper = objectMapper;
    this.oAuth2UserService = oAuth2UserService;
  }

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
    configuration.setAllowedOrigins(List.of("http://localhost:3000")); // 프론트엔드 URL
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token"));
    configuration.setExposedHeaders(Arrays.asList("X-Auth-Token"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /** OAuth2 인증 요청 리포지토리 빈 설정 */
  @Bean
  public AuthorizationRequestRepository<OAuth2AuthorizationRequest>
      authorizationRequestRepository() {
    return new HttpCookieOAuth2AuthorizationRequestRepository(objectMapper);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

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
                    .requestMatchers("/env")
                    .permitAll()

                    // Swagger UI 관련 경로 모두 허용
                    .requestMatchers("/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**")
                    .permitAll()
                    .requestMatchers("/swagger-resources/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/webjars/**")
                    .permitAll()
                    .requestMatchers("/favicon.ico")
                    .permitAll()
                    .requestMatchers("/verification-success.html")
                    .permitAll()
                    .requestMatchers("/verification-failed.html")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
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
                                .authorizationRequestRepository(authorizationRequestRepository()))
                    .redirectionEndpoint(redirection -> redirection.baseUri("/login/oauth2/code/*"))
                    .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                    .successHandler(oAuth2AuthenticationSuccessHandler))

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
