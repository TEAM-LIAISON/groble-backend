// package liaison.groble.security.jwt;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
//
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import java.util.Vector;
// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.TimeUnit;
// import java.util.concurrent.atomic.AtomicInteger;
// import lombok.extern.slf4j.Slf4j;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.test.util.ReflectionTestUtils;
//
/// **
// * SecurityContext 격리 테스트
// * 다중 사용자 동시 접속 시 SecurityContext가 올바르게 격리되는지 확인
// */
// @Slf4j
// @ExtendWith(MockitoExtension.class)
// class SecurityContextIsolationTest {
//
//    @Mock
//    private JwtTokenProvider jwtTokenProvider;
//
//    @Mock
//    private UserDetailsService userDetailsService;
//
//    @Mock
//    private HttpServletRequest request;
//
//    @Mock
//    private HttpServletResponse response;
//
//    @Mock
//    private FilterChain filterChain;
//
//    private JwtAuthenticationFilter filter;
//
//    @BeforeEach
//    void setUp() {
//        filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
//        ReflectionTestUtils.setField(filter, "cookieDomain", "test.com");
//        ReflectionTestUtils.setField(filter, "serverEnv", "local");
//        SecurityContextHolder.clearContext();
//    }
//
//    /**
//     * HttpServletRequest mock 객체에 필요한 기본 설정을 추가하는 헬퍼 메서드
//     */
//    private void setupBasicMockRequest(HttpServletRequest mockRequest, String uri) {
//        when(mockRequest.getRequestURI()).thenReturn(uri);
//        when(mockRequest.getMethod()).thenReturn("GET");
//        when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080" +
// uri));
//        when(mockRequest.getServerName()).thenReturn("localhost");
//        when(mockRequest.getServerPort()).thenReturn(8080);
//        when(mockRequest.getScheme()).thenReturn("http");
//        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
//
//        // 헤더 설정
//        Vector<String> headerNames = new Vector<>();
//        headerNames.add("User-Agent");
//        headerNames.add("Accept");
//        when(mockRequest.getHeaderNames()).thenReturn(headerNames.elements());
//        when(mockRequest.getHeader("User-Agent")).thenReturn("MockBrowser/1.0");
//        when(mockRequest.getHeader("Accept")).thenReturn("application/json");
//
//        // shouldNotFilter 메서드를 위한 설정
//        when(mockRequest.getServletPath()).thenReturn("");
//        when(mockRequest.getPathInfo()).thenReturn(uri);
//    }
//
//    @Test
//    void 동시_다중_사용자_요청시_SecurityContext가_격리되어야_함() throws Exception {
//        // Given
//        int numberOfUsers = 10;
//        ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
//        CountDownLatch startLatch = new CountDownLatch(1);
//        CountDownLatch endLatch = new CountDownLatch(numberOfUsers);
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failureCount = new AtomicInteger(0);
//
//        // JwtTokenProvider가 모든 토큰에 대해 유효성 검증을 통과하도록 설정
//        when(jwtTokenProvider.parseClaimsJws(anyString(), any(TokenType.class))).thenReturn(null);
//
//        // 각 사용자별 토큰과 UserDetails 설정
//        for (int i = 1; i <= numberOfUsers; i++) {
//            final int userId = i;
//            final String token = "token" + userId;
//            final String email = "user" + userId + "@test.com";
//
//            UserDetails userDetails = User.builder()
//                .username(String.valueOf(userId))
//                .password("password")
//                .authorities("ROLE_USER")
//                .build();
//
//            when(jwtTokenProvider.getUserId(token, TokenType.ACCESS)).thenReturn((long) userId);
//            when(jwtTokenProvider.getEmail(token, TokenType.ACCESS)).thenReturn(email);
//
// when(userDetailsService.loadUserByUsername(String.valueOf(userId))).thenReturn(userDetails);
//        }
//
//        // When - 동시에 여러 사용자 요청 처리
//        for (int i = 1; i <= numberOfUsers; i++) {
//            final int userId = i;
//            final String token = "token" + userId;
//
//            executor.submit(() -> {
//                try {
//                    startLatch.await(); // 모든 스레드가 동시에 시작
//
//                    // 각 요청마다 새로운 mock 객체 생성
//                    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
//                    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
//                    FilterChain mockChain = mock(FilterChain.class);
//
//                    // 기본 mock 설정
//                    setupBasicMockRequest(mockRequest, "/api/test");
//
//                    // 토큰 설정
//                    Cookie[] cookies = new Cookie[] { new Cookie("accessToken", token) };
//                    when(mockRequest.getCookies()).thenReturn(cookies);
//
//                    // 필터 실행
//                    filter.doFilterInternal(mockRequest, mockResponse, mockChain);
//
//                    // 인증 정보 확인 - filterChain 실행 전의 SecurityContext 상태를 확인해야 함
//                    // 실제 필터는 finally 블록에서 SecurityContext를 클리어하므로
//                    // 여기서는 mock verify를 통해 간접적으로 확인
//                    verify(mockChain).doFilter(mockRequest, mockResponse);
//                    successCount.incrementAndGet();
//                    log.info("사용자 {} 처리 성공", userId);
//
//                } catch (Exception e) {
//                    failureCount.incrementAndGet();
//                    log.error("사용자 {} 처리 중 오류", userId, e);
//                } finally {
//                    endLatch.countDown();
//                }
//            });
//        }
//
//        // 모든 스레드 동시 시작
//        startLatch.countDown();
//
//        // 모든 스레드 완료 대기
//        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
//        executor.shutdown();
//
//        // Then
//        assertThat(completed).isTrue();
//        assertThat(successCount.get()).isEqualTo(numberOfUsers);
//        assertThat(failureCount.get()).isEqualTo(0);
//
//        log.info("테스트 완료 - 성공: {}, 실패: {}", successCount.get(), failureCount.get());
//    }
//
//    @Test
//    void SecurityContext가_요청_완료_후_클리어되어야_함() throws Exception {
//        // Given
//        String token = "testToken";
//        Long userId = 123L;
//
//        // 기본 mock 설정
//        setupBasicMockRequest(request, "/api/test");
//
//        Cookie[] cookies = new Cookie[] { new Cookie("accessToken", token) };
//        when(request.getCookies()).thenReturn(cookies);
//
//        when(jwtTokenProvider.parseClaimsJws(token, TokenType.ACCESS)).thenReturn(null);
//        when(jwtTokenProvider.getUserId(token, TokenType.ACCESS)).thenReturn(userId);
//
//        UserDetails userDetails = User.builder()
//            .username(userId.toString())
//            .password("password")
//            .authorities("ROLE_USER")
//            .build();
//        when(userDetailsService.loadUserByUsername(userId.toString())).thenReturn(userDetails);
//
//        // When
//        filter.doFilterInternal(request, response, filterChain);
//
//        // Then
//        // SecurityContext가 클리어되었는지 확인
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        assertThat(auth).isNull();
//
//        verify(filterChain).doFilter(request, response);
//    }
//
//    @Test
//    void 토큰이_없는_경우_인증없이_진행되어야_함() throws Exception {
//        // Given
//        setupBasicMockRequest(request, "/api/test");
//        when(request.getCookies()).thenReturn(null);
//        when(request.getHeader("Authorization")).thenReturn(null);
//
//        // When
//        filter.doFilterInternal(request, response, filterChain);
//
//        // Then
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        assertThat(auth).isNull();
//
//        verify(filterChain).doFilter(request, response);
//        verify(jwtTokenProvider, never()).parseClaimsJws(anyString(), any(TokenType.class));
//    }
//
//    @Test
//    void 만료된_액세스토큰과_유효한_리프레시토큰이_있을때_토큰이_재발급되어야_함() throws Exception {
//        // Given
//        String accessToken = "expiredAccessToken";
//        String refreshToken = "validRefreshToken";
//        Long userId = 456L;
//        String email = "test@example.com";
//
//        setupBasicMockRequest(request, "/api/test");
//
//        Cookie[] cookies = new Cookie[] {
//            new Cookie("accessToken", accessToken),
//            new Cookie("refreshToken", refreshToken)
//        };
//        when(request.getCookies()).thenReturn(cookies);
//
//        // 액세스 토큰은 만료됨
//        when(jwtTokenProvider.parseClaimsJws(accessToken, TokenType.ACCESS))
//            .thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));
//
//        // 리프레시 토큰은 유효함
//        when(jwtTokenProvider.parseClaimsJws(refreshToken, TokenType.REFRESH)).thenReturn(null);
//        when(jwtTokenProvider.getUserId(refreshToken, TokenType.REFRESH)).thenReturn(userId);
//        when(jwtTokenProvider.getEmail(refreshToken, TokenType.REFRESH)).thenReturn(email);
//
//        // 새 액세스 토큰 생성
//        String newAccessToken = "newAccessToken";
//        when(jwtTokenProvider.createAccessTokenWithRefreshConstraint(userId, email, refreshToken))
//            .thenReturn(newAccessToken);
//        when(jwtTokenProvider.getRefreshTokenExpirationInstant(refreshToken))
//            .thenReturn(java.time.Instant.now().plusSeconds(3600));
//        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(1800000L);
//
//        // UserDetails 설정
//        UserDetails userDetails = User.builder()
//            .username(userId.toString())
//            .password("password")
//            .authorities("ROLE_USER")
//            .build();
//        when(userDetailsService.loadUserByUsername(userId.toString())).thenReturn(userDetails);
//        when(jwtTokenProvider.getUserId(newAccessToken, TokenType.ACCESS)).thenReturn(userId);
//
//        // When
//        filter.doFilterInternal(request, response, filterChain);
//
//        // Then
//        verify(response).addHeader("X-Token-Refresh-Status", "success");
//        verify(filterChain).doFilter(request, response);
//
//        // 새 토큰으로 인증이 설정되었는지 간접적으로 확인
//        verify(userDetailsService).loadUserByUsername(userId.toString());
//    }
// }
