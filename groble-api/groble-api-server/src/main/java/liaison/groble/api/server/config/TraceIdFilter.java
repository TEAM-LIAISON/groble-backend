package liaison.groble.api.server.config;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Populates the logging {@link MDC} with correlation data for every HTTP request.
 *
 * <p>The filter aligns with common enterprise logging practices by:
 *
 * <ul>
 *   <li>Reusing incoming trace/span identifiers (HTTP headers) when present.
 *   <li>Generating identifiers when missing and writing them back to the response.
 *   <li>Capturing request-scoped attributes (HTTP method, URI, client IP, user agent) for
 *       structured logging.
 * </ul>
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

  static final String TRACE_ID_HEADER = "X-Trace-Id";
  static final String SPAN_ID_HEADER = "X-Span-Id";

  static final String MDC_KEY_TRACE_ID = "traceId";
  static final String MDC_KEY_SPAN_ID = "spanId";
  static final String MDC_KEY_HTTP_METHOD = "httpMethod";
  static final String MDC_KEY_REQUEST_URI = "requestUri";
  static final String MDC_KEY_CLIENT_IP = "clientIp";
  static final String MDC_KEY_USER_AGENT = "userAgent";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String traceId = resolveHeaderOrDefault(request, TRACE_ID_HEADER);
    String spanId = resolveHeaderOrDefault(request, SPAN_ID_HEADER, this::generateSpanId);

    MDC.put(MDC_KEY_TRACE_ID, traceId);
    MDC.put(MDC_KEY_SPAN_ID, spanId);
    MDC.put(MDC_KEY_HTTP_METHOD, request.getMethod());
    MDC.put(MDC_KEY_REQUEST_URI, buildRequestUriWithQuery(request));
    MDC.put(MDC_KEY_CLIENT_IP, resolveClientIp(request));

    String userAgent = request.getHeader("User-Agent");
    if (StringUtils.hasText(userAgent)) {
      MDC.put(MDC_KEY_USER_AGENT, userAgent);
    }

    response.setHeader(TRACE_ID_HEADER, traceId);
    response.setHeader(SPAN_ID_HEADER, spanId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY_TRACE_ID);
      MDC.remove(MDC_KEY_SPAN_ID);
      MDC.remove(MDC_KEY_HTTP_METHOD);
      MDC.remove(MDC_KEY_REQUEST_URI);
      MDC.remove(MDC_KEY_CLIENT_IP);
      MDC.remove(MDC_KEY_USER_AGENT);
    }
  }

  private String resolveHeaderOrDefault(HttpServletRequest request, String headerName) {
    return resolveHeaderOrDefault(request, headerName, this::newUuidWithoutHyphen);
  }

  private String resolveHeaderOrDefault(
      HttpServletRequest request, String headerName, Supplier<String> generator) {
    String value = request.getHeader(headerName);
    if (StringUtils.hasText(value)) {
      return value;
    }
    return generator.get();
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (StringUtils.hasText(forwarded)) {
      return forwarded.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (StringUtils.hasText(realIp)) {
      return realIp;
    }
    return request.getRemoteAddr();
  }

  private String generateSpanId() {
    // OpenTelemetry span identifiers are 16 hexadecimal characters.
    return newUuidWithoutHyphen().substring(0, 16);
  }

  private String newUuidWithoutHyphen() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private String buildRequestUriWithQuery(HttpServletRequest request) {
    String queryString = request.getQueryString();
    if (StringUtils.hasText(queryString)) {
      return request.getRequestURI() + "?" + queryString;
    }
    return request.getRequestURI();
  }
}
