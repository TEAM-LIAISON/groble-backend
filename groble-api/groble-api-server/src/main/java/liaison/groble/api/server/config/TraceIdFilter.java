package liaison.groble.api.server.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds a trace identifier to the logging MDC for every incoming HTTP request.
 *
 * <p>The filter will reuse an existing trace id from the X-Trace-Id header when present, otherwise
 * it generates a new identifier. The trace id is also written back to the response header so that
 * clients can correlate requests with server-side logs.
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

  static final String TRACE_ID_HEADER = "X-Trace-Id";
  static final String MDC_KEY_TRACE_ID = "traceId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String traceId = request.getHeader(TRACE_ID_HEADER);
    if (!StringUtils.hasText(traceId)) {
      traceId = UUID.randomUUID().toString().replace("-", "");
    }

    MDC.put(MDC_KEY_TRACE_ID, traceId);
    response.setHeader(TRACE_ID_HEADER, traceId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY_TRACE_ID);
    }
  }
}
