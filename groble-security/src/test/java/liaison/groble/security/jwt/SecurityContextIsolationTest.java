package liaison.groble.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import lombok.extern.slf4j.Slf4j;

/** SecurityContext 격리 테스트 다중 사용자 동시 접속 시 SecurityContext가 올바르게 격리되는지 확인 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class SecurityContextIsolationTest {

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private UserDetailsService userDetailsService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    ReflectionTestUtils.setField(filter, "cookieDomain", "test.com");
    ReflectionTestUtils.setField(filter, "serverEnv", "local");
    SecurityContextHolder.clearContext();
  }

  @Test
  void 동시_다중_사용자_요청시_SecurityContext가_격리되어야_함() throws Exception {
    // Given
    int numberOfUsers = 10;
    ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(numberOfUsers);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // 각 사용자별 토큰과 UserDetails 설정
    for (int i = 1; i <= numberOfUsers; i++) {
      final int userId = i;
      final String token = "token" + userId;
      final String email = "user" + userId + "@test.com";

      UserDetails userDetails =
          User.builder()
              .username(String.valueOf(userId))
              .password("password")
              .authorities("ROLE_USER")
              .build();

      when(jwtTokenProvider.getUserId(token, TokenType.ACCESS)).thenReturn((long) userId);
      when(jwtTokenProvider.getEmail(token, TokenType.ACCESS)).thenReturn(email);
      when(userDetailsService.loadUserByUsername(String.valueOf(userId))).thenReturn(userDetails);
    }

    // When - 동시에 여러 사용자 요청 처리
    for (int i = 1; i <= numberOfUsers; i++) {
      final int userId = i;
      final String token = "token" + userId;

      executor.submit(
          () -> {
            try {
              startLatch.await(); // 모든 스레드가 동시에 시작

              // 각 요청마다 새로운 mock 객체 생성
              HttpServletRequest mockRequest = mock(HttpServletRequest.class);
              HttpServletResponse mockResponse = mock(HttpServletResponse.class);
              FilterChain mockChain = mock(FilterChain.class);

              // 토큰 설정
              Cookie[] cookies = new Cookie[] {new Cookie("accessToken", token)};
              when(mockRequest.getCookies()).thenReturn(cookies);
              when(mockRequest.getRequestURI()).thenReturn("/api/test");

              // 필터 실행
              filter.doFilterInternal(mockRequest, mockResponse, mockChain);

              // 인증 정보 확인
              Authentication auth = SecurityContextHolder.getContext().getAuthentication();
              if (auth != null && auth.isAuthenticated()) {
                String authenticatedUserId = auth.getName();
                if (String.valueOf(userId).equals(authenticatedUserId)) {
                  successCount.incrementAndGet();
                  log.info("사용자 {} 인증 성공", userId);
                } else {
                  failureCount.incrementAndGet();
                  log.error("사용자 {} 인증 실패: 잘못된 사용자 ID {}", userId, authenticatedUserId);
                }
              } else {
                failureCount.incrementAndGet();
                log.error("사용자 {} 인증 실패: 인증 정보 없음", userId);
              }

            } catch (Exception e) {
              failureCount.incrementAndGet();
              log.error("사용자 {} 처리 중 오류", userId, e);
            } finally {
              endLatch.countDown();
            }
          });
    }

    // 모든 스레드 동시 시작
    startLatch.countDown();

    // 모든 스레드 완료 대기
    boolean completed = endLatch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    // Then
    assertThat(completed).isTrue();
    assertThat(successCount.get()).isEqualTo(numberOfUsers);
    assertThat(failureCount.get()).isEqualTo(0);

    log.info("테스트 완료 - 성공: {}, 실패: {}", successCount.get(), failureCount.get());
  }

  @Test
  void SecurityContext가_요청_완료_후_클리어되어야_함() throws Exception {
    // Given
    String token = "testToken";
    Long userId = 123L;

    Cookie[] cookies = new Cookie[] {new Cookie("accessToken", token)};
    when(request.getCookies()).thenReturn(cookies);
    when(request.getRequestURI()).thenReturn("/api/test");

    when(jwtTokenProvider.parseClaimsJws(token, TokenType.ACCESS)).thenReturn(null);
    when(jwtTokenProvider.getUserId(token, TokenType.ACCESS)).thenReturn(userId);

    UserDetails userDetails =
        User.builder()
            .username(userId.toString())
            .password("password")
            .authorities("ROLE_USER")
            .build();
    when(userDetailsService.loadUserByUsername(userId.toString())).thenReturn(userDetails);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    // SecurityContext가 클리어되었는지 확인
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNull();

    verify(filterChain).doFilter(request, response);
  }
}
