package liaison.groble.security.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import liaison.groble.security.jwt.JwtAuthenticationFilter;
import liaison.groble.security.jwt.UserDetailsServiceImpl;
import liaison.groble.security.oauth2.handler.OAuth2AuthenticationSuccessHandler;

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
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

  public SecurityConfig(
      UserDetailsServiceImpl userDetailsService,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
      JwtTokenAuthenticationEntryPoint jwtTokenAuthenticationEntryPoint,
      JwtTokenAccessDeniedHandler jwtTokenAccessDeniedHandler,
      OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService) {
    this.userDetailsService = userDetailsService;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    this.jwtTokenAuthenticationEntryPoint = jwtTokenAuthenticationEntryPoint;
    this.jwtTokenAccessDeniedHandler = jwtTokenAccessDeniedHandler;
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

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(
        List.of("https://*.groble.im", "http://localhost:3000", "http://localhost:3001"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    // 이 부분 수정: 더 많은 헤더 허용
    configuration.setAllowedHeaders(
        Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Auth-Token",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Headers",
            "Accept",
            "Origin",
            "Cache-Control",
            "X-Requested-With"));

    // 필요시 더 많은 헤더 노출
    configuration.setExposedHeaders(Arrays.asList("X-Auth-Token", "Authorization"));
    configuration.setAllowCredentials(true);

    // 필요시 preflight 요청 캐시 시간 설정 (선택사항)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
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
                    .requestMatchers("/test-external")
                    .permitAll()
                    .requestMatchers(
                        "/api/v1/auth/sign-up",
                        "/api/v1/auth/sign-in",
                        "/api/v1/auth/sign-in/local/test",
                        "/api/v1/auth/email-verification/sign-up",
                        "/api/v1/auth/verify-code/sign-up",
                        "/api/v1/auth/phone-number/verify-request",
                        "/api/v1/auth/phone-number/verify-code",
                        "/api/v1/auth/password/reset-request",
                        "/api/v1/auth/password/reset",
                        "/api/v1/market/contents/**",
                        "/api/v1/market/intro/**",
                        "/api/v1/market/maker-info/**")
                    .permitAll()
                    .requestMatchers("/api/v1/oauth2/**")
                    .permitAll()
                    .requestMatchers("/oauth2/**")
                    .permitAll()
                    .requestMatchers("/api/v1/me")
                    .permitAll()
                    .requestMatchers("/api/v1/home/contents")
                    .permitAll()
                    .requestMatchers("/api/v1/content/*/pay/*")
                    .permitAll()
                    .requestMatchers("/payple-payment")
                    .permitAll()
                    .requestMatchers("/payment/**")
                    .permitAll()
                    .requestMatchers("/api/v1/groble/contents")
                    .permitAll()
                    .requestMatchers(
                        new RegexRequestMatcher("^/api/v1/content/\\d+$", HttpMethod.GET.name()))
                    .permitAll()
                    .requestMatchers("/api/v1/content/maker-info/**")
                    .permitAll()
                    .requestMatchers(
                        new RegexRequestMatcher(
                            "^/api/v1/market/view/[^/]+$", HttpMethod.POST.name()))
                    .permitAll()
                    .requestMatchers(
                        new RegexRequestMatcher(
                            "^/api/v1/content/view/\\d+$", HttpMethod.POST.name()))
                    .permitAll()
                    .requestMatchers("/api/v1/content/**/reviews")
                    .permitAll()
                    .requestMatchers("/api/v1/contents/document/category")
                    .permitAll()
                    .requestMatchers("/api/v1/contents/coaching/category")
                    .permitAll()
                    .requestMatchers("/api/v1/payments/**")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/nickname/check")
                    .permitAll()
                    .requestMatchers(
                        "/api/v1/guest/auth/code-request", "/api/v1/guest/auth/verify-request")
                    .permitAll()
                    .requestMatchers("/login/**")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .requestMatchers("/")
                    .permitAll()
                    .requestMatchers("/env")
                    .permitAll()
                    // [관리자 API 접근 허용 설정]
                    .requestMatchers("/api/v1/admin/auth/sign-in")
                    .permitAll()
                    // [변경된 회원가입 및 로그인 플로우 적용]
                    .requestMatchers(
                        "/api/v1/auth/integrated/sign-up",
                        "/api/v1/auth/integrated/sign-in",
                        "/api/v1/verification/email/code/sign-up",
                        "/api/v1/verification/email/code/verify/sign-up",
                        "/api/v1/verification/email/code/password-reset",
                        "/api/v1/verification/password/reset")
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
                    .anyRequest()
                    .authenticated())

        // OAuth2 로그인 설정
        .oauth2Login(
            oauth2 ->
                oauth2
                    .authorizationEndpoint(
                        authorization -> authorization.baseUri("/oauth2/authorize"))
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
